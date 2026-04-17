---
name: media3-live-streaming
description: Use this skill to ship live streaming in an Android app with AndroidX Media3 1.10.0. Use this skill to tune MediaItem.LiveConfiguration for target, min, and max live offsets, keep playback speed inside the recovery window, recover from ERROR_CODE_BEHIND_LIVE_WINDOW, drive DVR scrubbing when the stream has a live window, handle catch-up playback from the start of the window, and keep UI state coherent across connecting, live, stalled, reconnecting, and ended states.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - live
    - hls
    - dash
    - liveconfiguration
    - dvr
    - live-offset
    - behind-live-window
    - catch-up

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** include `media3-exoplayer-hls` for HLS live, `media3-exoplayer-dash` for DASH live, or both.
- Project **MUST NOT** pin playback speed at exactly `1.0f` on a live stream.

## Step 1: plan

1. Enumerate every live stream URL. Group by protocol and by presence of a DVR window.
2. For each live source, record the target live offset the backend recommends (often 3 to 15 seconds).
3. Decide whether the UI exposes scrubbing across the DVR window.
4. Identify every place the app constructs `ExoPlayer`.
5. Confirm the error listener handles `ERROR_CODE_BEHIND_LIVE_WINDOW` explicitly.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer      = { module = "androidx.media3:media3-exoplayer",      version.ref = "media3" }
media3-exoplayer-hls  = { module = "androidx.media3:media3-exoplayer-hls",  version.ref = "media3" }
media3-exoplayer-dash = { module = "androidx.media3:media3-exoplayer-dash", version.ref = "media3" }
media3-session        = { module = "androidx.media3:media3-session",        version.ref = "media3" }
```

## Step 3: build the live MediaItem

### RIGHT

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes

val liveHls = MediaItem.Builder()
    .setUri("https://example.com/live.m3u8")
    .setMimeType(MimeTypes.APPLICATION_M3U8)
    .setLiveConfiguration(
        MediaItem.LiveConfiguration.Builder()
            .setTargetOffsetMs(6_000)
            .setMinOffsetMs(2_000)
            .setMaxOffsetMs(30_000)
            .setMinPlaybackSpeed(0.97f)
            .setMaxPlaybackSpeed(1.03f)
            .build()
    )
    .build()

player.setMediaItem(liveHls)
player.prepare()
player.playWhenReady = true
```

### WRONG

```kotlin
// WRONG: no LiveConfiguration means Media3 falls back to defaults
val liveHls = MediaItem.fromUri("https://example.com/live.m3u8")
```

```kotlin
// WRONG: pinning playbackSpeed to 1.0 kills latency recovery
player.setPlaybackSpeed(1.0f)
```

## Step 4: seek to the live edge

```kotlin
fun snapToLive(player: Player) {
    player.seekToDefaultPosition()
}
```

## Step 5: expose a DVR scrubbing UI

```kotlin
import androidx.media3.common.Timeline

fun dvrWindow(player: Player): Timeline.Window? {
    if (player.currentTimeline.isEmpty) return null
    val window = Timeline.Window()
    player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
    return window.takeIf { it.isDynamic && it.isSeekable }
}
```

## Step 6: handle BEHIND_LIVE_WINDOW

### RIGHT

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                player.seekToDefaultPosition()
                player.prepare()
            }
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> retryLiveWithBackoff()
            else -> analytics.logPlayerError(error)
        }
    }
})
```

## Step 7: catch up from the start of the window

```kotlin
fun startOver(player: Player) {
    if (player.currentTimeline.isEmpty) return
    val window = Timeline.Window()
    player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
    player.seekTo(player.currentMediaItemIndex, window.defaultPositionMs.coerceAtLeast(0))
}
```

**DO NOT** call `player.seekTo(0)` on a live stream.

## Step 8: UI state machine

| UI state | Signals |
|---|---|
| Connecting | `playbackState == STATE_BUFFERING`, `playWhenReady` just set to `true`, no frame rendered yet |
| Live | `playbackState == STATE_READY`, `isPlaying == true`, position within the default offset band |
| Stalled | `STATE_BUFFERING` while `isPlaying` was already `true` |
| Reconnecting | Last error was `ERROR_CODE_IO_*` and the app is retrying |
| Ended | `playbackState == STATE_ENDED` or an unrecoverable error surfaced |

## Step 9: retry and reconnect

```kotlin
import kotlinx.coroutines.*

class LiveRetry(private val scope: CoroutineScope) {
    private var attempt = 0
    private var job: Job? = null

    fun onNetworkError(player: Player) {
        attempt++
        val delayMs = (500L shl minOf(attempt, 5)).coerceAtMost(15_000L)
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            player.seekToDefaultPosition()
            player.prepare()
        }
    }

    fun reset() { attempt = 0; job?.cancel() }
}
```

## Step 10: session and background behavior

```kotlin
override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
    if (player.currentTimeline.isEmpty.not()) player.seekToDefaultPosition()
    return mediaSession
}
```

## Common pitfalls

- **No `LiveConfiguration`.** Falls back to defaults that are wrong for most streams.
- **Pinning `playbackSpeed = 1.0f`.** Disables Media3's latency recovery.
- **Using `player.duration` or `seekTo(0)` on live.**
- **Ignoring `ERROR_CODE_BEHIND_LIVE_WINDOW`.**
- **Treating Stalled and Connecting the same in the UI.**
- **Tight retry loop on network errors.**
- **Saving live position for resume.**
- **Scrubbing back and also calling `seekToDefaultPosition`.**
