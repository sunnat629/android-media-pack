---
name: media3-live-only-streaming
description: Use this skill to ship live-only streaming with AndroidX Media3 1.9.0, where the stream has no DVR window and seeking is disabled. Use this skill to disable scrubbing UI for non-seekable windows, pin the player to the live edge, treat every pause-resume as a snap-to-live, surface a dedicated reconnect state, and configure LiveConfiguration with a tight offset budget.
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
- Project **MUST** pin Media3 to **1.9.0** or later.
- Project **MUST** first adopt the `media3-live-streaming` skill. Live-only is a specialization of it.
- Project **MUST NOT** render a scrub bar or DVR timeline for live-only content.
- Project **MUST NOT** save resume positions for live-only items. Resume semantics do not exist.

## Step 1: plan

1. Classify every live stream: DVR-capable vs live-only. Streams where the manifest's `Timeline.Window` reports `isSeekable == false` are live-only.
2. Identify UI differences from DVR live: no scrubber, no 10s skip, no chapter marks, no resume.
3. Plan a "behind live window" recovery path. Live-only has no buffer to seek into, so the recovery is always snap-to-live.
4. Identify every pause interaction. For sports, chat, or auction streams, pause often means "leave for a moment and return to live". Respect that.
5. Confirm analytics treats live-only as its own content class.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"

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

Tighter offset budget than DVR live: the stream does not have a window to catch up from, so latency recovery has to be quick.

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

Use this to drive UI mode switches between DVR-live and live-only.

## Step 5: hide the scrubber UI

When `isLiveOnly == true`:

- Hide the seek bar.
- Hide the 10s skip-back / skip-forward buttons.
- Replace the time display with a red "LIVE" indicator.
- Keep the pause / play toggle.

```kotlin
@Composable
fun LiveOnlyChrome(player: Player) {
    Row {
        PlayPauseButton(player)
        LiveBadge()
        Spacer(Modifier.weight(1f))
        OverflowMenu()
    }
}
```

**DO NOT** render a disabled scrubber. Users will try to drag it.

## Step 6: pause means snap-to-live on resume

### RIGHT

```kotlin
player.addListener(object : Player.Listener {
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying && isLiveOnly(player)) {
            player.seekToDefaultPosition()
        }
    }
})
```

### WRONG

```kotlin
// WRONG: resuming from the buffered pause point puts the user behind live, but the manifest
// no longer has those segments, so playback stalls immediately
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

For live-only, this is the only valid recovery. There is no "start over" button.

## Step 8: reconnect state

When the network drops and reconnects, live-only cannot resume from the stored position. The UI **MUST** show a "Reconnecting" overlay, then snap to live once `STATE_READY` fires.

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

For a live-only stream, backgrounding typically means "stop playing". Some verticals (audio-first live) prefer "keep playing in the background". Pick one and document it.

**DO NOT** save a resume position for live-only items. There is nothing meaningful to resume to.

## Step 10: analytics

Treat live-only as its own content class in analytics. Key metrics:

- Time to live (latency between event and client).
- Reconnect count per session.
- Join latency.
- Drop-off by minute.

Do **NOT** share the DVR-live analytics bucket. The product team will be misled about viewer intent.

## Common pitfalls

- **DVR-style scrubber UI on live-only.** Users drag, get no response, assume broken.
- **Using DVR offset budgets.** Puts viewer too far behind the event.
- **Saving resume position.** Nothing to resume to.
- **Not snapping to live on resume.** Stall immediately because the segment is gone.
- **Generic error UI for network drops.** Reconnect state is distinct.
- **Sharing analytics with DVR live.** Misleads product.
- **Keeping the 10s skip-back button.** Not meaningful for live-only.
- **Pinning `playbackSpeed = 1.0f`.** Disables latency recovery, same as DVR live.
