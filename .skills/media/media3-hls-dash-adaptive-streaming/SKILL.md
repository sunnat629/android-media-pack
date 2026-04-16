---
name: media3-hls-dash-adaptive-streaming
description: Use this skill to configure HLS and DASH adaptive streaming in an Android app using AndroidX Media3 1.9.0. Use this skill to construct HlsMediaSource and DashMediaSource through DefaultMediaSourceFactory, tune TrackSelectionParameters for adaptive bitrate selection, handle live versus VOD playback behavior, configure buffering and bandwidth estimation, and avoid common pitfalls with HLS variants, DASH representations, and subtitle track selection.
license: Complete terms in LICENSE.txt
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.9.0"
  last_reviewed: "2026-04-16"
  keywords:
    - android
    - media3
    - hls
    - dash
    - adaptive-streaming
    - abr
    - trackselectionparameters
    - bandwidthmeter
    - live
    - vod

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.9.0** or later.
- Project **MUST** declare `media3-exoplayer-hls` for HLS and `media3-exoplayer-dash` for DASH. Declaring one does not transitively pull in the other.
- Project **MUST NOT** construct `HlsMediaSource.Factory` or `DashMediaSource.Factory` directly on the `ExoPlayer.Builder`. Use `DefaultMediaSourceFactory` with the correct MIME type on each `MediaItem`.
- Low-latency HLS is scoped out of v1.x. It will land in a future `media3-low-latency-live` skill.

## Step 1: plan

1. Enumerate every stream URL. Group by protocol (HLS, DASH, SmoothStreaming, progressive).
2. Confirm each manifest is reachable over HTTPS with a plausible `User-Agent`.
3. Decide whether the app needs adaptive video selection across the full ladder, or whether a single quality is pinned by product policy.
4. Identify the maximum display resolution per device class. `TrackSelectionParameters.setMaxVideoSize` bounds will prevent wasted bandwidth.
5. For live streams, decide a live offset target (typically 3 to 15 seconds) and whether time-shifting to the DVR window is required.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"

[libraries]
media3-exoplayer      = { module = "androidx.media3:media3-exoplayer",      version.ref = "media3" }
media3-exoplayer-hls  = { module = "androidx.media3:media3-exoplayer-hls",  version.ref = "media3" }
media3-exoplayer-dash = { module = "androidx.media3:media3-exoplayer-dash", version.ref = "media3" }
media3-datasource-okhttp = { module = "androidx.media3:media3-datasource-okhttp", version.ref = "media3" }
```

## Step 3: construct the player with DefaultMediaSourceFactory

### RIGHT

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

val http = DefaultHttpDataSource.Factory()
    .setUserAgent("Shunnek/1.0")
    .setConnectTimeoutMs(15_000)

val mediaSourceFactory = DefaultMediaSourceFactory(context)
    .setDataSourceFactory(http)

val player = ExoPlayer.Builder(context)
    .setMediaSourceFactory(mediaSourceFactory)
    .build()

val hls = MediaItem.Builder()
    .setUri("https://example.com/master.m3u8")
    .setMimeType(MimeTypes.APPLICATION_M3U8)
    .build()

val dash = MediaItem.Builder()
    .setUri("https://example.com/manifest.mpd")
    .setMimeType(MimeTypes.APPLICATION_MPD)
    .build()

player.setMediaItems(listOf(hls, dash))
player.prepare()
```

### WRONG

```kotlin
// WRONG: hand-building HlsMediaSource.Factory sidesteps DefaultMediaSourceFactory's adaptive wiring
val hlsSource = HlsMediaSource.Factory(http).createMediaSource(MediaItem.fromUri(url))
val player = ExoPlayer.Builder(context).build()
player.setMediaSource(hlsSource)
```

## Step 4: tune TrackSelectionParameters for ABR

### RIGHT

```kotlin
import androidx.media3.common.TrackSelectionParameters

player.trackSelectionParameters = TrackSelectionParameters.Builder(context)
    .setMaxVideoSize(1920, 1080)
    .setMaxVideoBitrate(6_000_000)
    .setPreferredAudioLanguage("en")
    .setSelectTextByDefault(false)
    .build()
```

### WRONG

```kotlin
// WRONG: rolling a custom TrackSelectionOverride for ABR breaks on manifest updates and wastes battery
val override = TrackSelectionOverride(trackGroup, listOf(0, 1, 2))
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon().addOverride(override).build()
```

**DO NOT** set `setMaxVideoBitrate` below the lowest ladder rung. Media3 will fail to resolve any variant and surface `ERROR_CODE_IO_NO_PERMISSION` or an empty track list.

## Step 5: handle live vs VOD

### RIGHT

```kotlin
import androidx.media3.common.MediaItem

val live = MediaItem.Builder()
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
```

**DO NOT** pin live playback to `playbackSpeed = 1.0f`. The player uses small speed adjustments to recover latency drift. Pinning it disables that recovery.

## Step 6: select subtitles with selectTextByDefault

Media3 1.9.0 replaces hand-rolled subtitle overrides with a boolean on `TrackSelectionParameters`.

### RIGHT

```kotlin
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon()
    .setSelectTextByDefault(true)
    .setPreferredTextLanguage("en")
    .build()
```

### WRONG

```kotlin
// WRONG: synthesizing a TrackSelectionOverride to toggle subtitles ignores manifest changes
val override = TrackSelectionOverride(textTrackGroup, listOf(0))
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon().addOverride(override).build()
```

## Step 7: bandwidth and buffering

**PREFERRED** defaults are the `DefaultBandwidthMeter` and `DefaultLoadControl`. Override only when you have measured evidence of suboptimal behavior.

```kotlin
import androidx.media3.exoplayer.DefaultLoadControl

val loadControl = DefaultLoadControl.Builder()
    .setBufferDurationsMs(
        /* minBufferMs = */ 30_000,
        /* maxBufferMs = */ 60_000,
        /* bufferForPlaybackMs = */ 2_500,
        /* bufferForPlaybackAfterRebufferMs = */ 5_000,
    )
    .build()

val player = ExoPlayer.Builder(context)
    .setLoadControl(loadControl)
    .setMediaSourceFactory(mediaSourceFactory)
    .build()
```

**DO NOT** set `maxBufferMs` above 5 minutes on a mobile app. It starves memory and trips the 1.9.0 PreloadManager memory guard.

## Step 8: handle HLS and DASH errors distinctly

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> reportBadManifest(error)
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> retryWithBackoff()
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> player.seekToDefaultPosition()
            else -> analytics.logPlayerError(error)
        }
    }
})
```

## Common pitfalls

- **Missing MIME type on the `MediaItem`.** Without `MimeTypes.APPLICATION_M3U8` or `APPLICATION_MPD`, `DefaultMediaSourceFactory` falls back to progressive parsing and fails.
- **Forgetting the protocol module.** `media3-exoplayer-hls` or `media3-exoplayer-dash` is required on the classpath.
- **Hand-building `MediaSource.Factory` instances.** Bypasses DRM wiring and load-control hookup done by `DefaultMediaSourceFactory`.
- **Low `setMaxVideoBitrate`.** A bound below the lowest variant resolves to zero playable tracks.
- **Pinning `playbackSpeed = 1.0f` on live.** Disables latency recovery.
- **Manual subtitle `TrackSelectionOverride`.** Prefer `setSelectTextByDefault` plus `setPreferredTextLanguage`.
- **Oversized `maxBufferMs`.** Starves memory and trips the 1.9.0 PreloadManager memory guard.
- **Ignoring `ERROR_CODE_BEHIND_LIVE_WINDOW`.** The player must be seeked back into the live window to recover.

## Checklist

- [ ] `media3-exoplayer-hls` and `media3-exoplayer-dash` on the classpath as needed.
- [ ] All protected and adaptive `MediaItem` instances set `setMimeType` correctly.
- [ ] `DefaultMediaSourceFactory` is the single source factory.
- [ ] `TrackSelectionParameters` bounds video size, bitrate, and preferred languages.
- [ ] Live streams configure `LiveConfiguration` with sensible target, min, and max offsets.
- [ ] `setSelectTextByDefault` drives subtitle on or off, not custom overrides.
- [ ] `DefaultLoadControl` buffer durations are within mobile-appropriate ranges.
- [ ] `Player.Listener.onPlayerError` branches on `PARSING_MANIFEST_MALFORMED`, `IO_NETWORK_CONNECTION_FAILED`, and `BEHIND_LIVE_WINDOW`.
- [ ] Smoke test covers VOD HLS, VOD DASH, live HLS, live DASH, and a subtitle toggle.
