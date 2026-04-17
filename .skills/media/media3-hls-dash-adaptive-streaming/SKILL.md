---
name: media3-hls-dash-adaptive-streaming
description: Use this skill to configure HLS and DASH adaptive streaming in an Android app using AndroidX Media3 1.10.0. Use this skill to construct HlsMediaSource and DashMediaSource through DefaultMediaSourceFactory, tune TrackSelectionParameters for adaptive bitrate selection, handle live versus VOD playback behavior, configure buffering and bandwidth estimation, and avoid common pitfalls with HLS variants, DASH representations, and subtitle track selection.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
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
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** declare `media3-exoplayer-hls` for HLS and `media3-exoplayer-dash` for DASH.
- Project **MUST NOT** construct `HlsMediaSource.Factory` or `DashMediaSource.Factory` directly.

## Step 1: plan

1. Enumerate every stream URL.
2. Confirm each manifest is reachable over HTTPS.
3. Decide whether the app needs adaptive video selection.
4. Identify the maximum display resolution per device class.
5. For live streams, decide a live offset target.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

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
// WRONG: rolling a custom TrackSelectionOverride for ABR breaks on manifest updates
val override = TrackSelectionOverride(trackGroup, listOf(0, 1, 2))
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon().addOverride(override).build()
```

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

## Step 6: select subtitles with selectTextByDefault

### RIGHT

```kotlin
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon()
    .setSelectTextByDefault(true)
    .setPreferredTextLanguage("en")
    .build()
```

## Step 7: bandwidth and buffering

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

**DO NOT** set `maxBufferMs` above 5 minutes on a mobile app. It starves memory and trips the 1.10.0 PreloadManager memory guard.

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

- **Missing MIME type on the `MediaItem`.**
- **Forget