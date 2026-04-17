---
name: media3-live-only-streaming
description: Use this skill to ship live-only streaming with AndroidX Media3 1.10.0, where the stream has no DVR window and seeking is disabled. Use this skill to disable scrubbing UI for non-seekable windows, pin the player to the live edge, treat every pause-resume as a snap-to-live, surface a dedicated reconnect state, and configure LiveConfiguration with a tight offset budget.
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
    - live-only
    - no-dvr
    - liveconfiguration
    - reconnect
    - snap-to-live

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** first adopt the `media3-live-streaming` skill.
- Project **MUST NOT** render a scrub bar or DVR timeline for live-only content.
- Project **MUST NOT** save resume positions for live-only items.

## Step 1: plan

1. Classify every live stream: DVR-capable vs live-only.
2. Identify UI differences from DVR live.
3. Plan a "behind live window" recovery path.
4. Identify every pause interaction.
5. Confirm analytics treats live-only as its own content class.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer      = { module = "androidx.media3:media3-exoplayer",      version.ref = "media3" }
media3-exoplayer-hls  = { module = "androidx.media3:media3-exoplayer-hls",  version.ref = "media3" }
```

## Step 3: build a live-only MediaItem

### RIGHT

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes

val liveOnly = MediaItem.Builder()
    .setUri("https://example.com/live-only.m3u8")
    .setMimeType(MimeTypes.APPLICATION_M3U8)
    .setLiveConfiguration(
        MediaItem.LiveConfiguration.Builder()
            .setTargetOffsetMs(3_000)
            .setMinOffsetMs(1_500)
            .setMaxOffsetMs(8_000)
            .setMinPlaybackSpeed(0.97f)
            .setMaxPlaybackSpeed(1.03f)
            .build()
    )
    .build()

player.setMediaItem(liveOnly)
player.prepare()
player.playWhenReady = true
```

### WRONG

```kotlin
// WRONG: using DVR-live offsets (10-30s) on a live-only stream puts the viewer far behind the event
.setTargetOffsetMs(15_000)
.setMaxOffsetMs(30_000)
```

## Step 4: detect live-only from the Timeline

```kotlin
import androidx.media3.common.Player
import androidx.media3.common.Timeline

fun isLiveOnly(player: Player): Boolean {
    if (player.currentTimeline.isEmpty) return false
    val window = Timeline.Window()
    player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
    return window.isDynamic && !window.isSeekable
}
```

## Step 5: hide the scrubber UI

When `isLiveOnly == true`:

- Hide the seek bar.
- Hide the 10s skip-back / skip-forward buttons.
- Replace the time display with a red "LIVE" indicator.
- Keep the pause / play toggle.

## Step 6: pause means snap-to-live on resume

```kotlin
player.addListener(object : Player.Listener {
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying && isLiveOnly(player)) {
            player.seekToDefaultPosition()
        }
    }
})
```

## Step 7: handle BEHIND_LIVE_WINDOW as snap-to-live

```kotlin
import androidx.media3.common.PlaybackException

override fun onPlayerError(error: PlaybackException) {
    if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
        player.seekToDefaultPosition()
        player.prepare()
    }
}
```

## Step 8: reconnect state

```kotlin
var uiState by remember { mutableStateOf(UiState.LIVE) }

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        uiState = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> UiState.RECONNECTING
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> UiState.RECONNECTING
            else -> UiState.ERROR
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_READY) uiState = UiState.LIVE
    }
})
```

## Step 9: background behavior

For a live-only stream, backgrounding typically means "stop playing".

## Step 10: analytics

Key metrics:

- Time to live.
- Reconnect count per session.
- Join latency.
- Drop-off by minute.

## Common pitfalls

- **DVR-style scrubber UI on live-only.**
- **Using DVR offset budgets.**
- **Saving resume position.**
- **Not snapping to live on resume.**
- **Generic error UI for network drops.**
- **Sharing analytics with DVR live.**
- **Keeping the 10s skip-back button.**
- **Pinning `playbackSpeed = 1.0f`.**
