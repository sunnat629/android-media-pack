---
name: media3-bandwidth-abr
description: Use this skill to configure bandwidth estimation and adaptive bitrate (ABR) behavior in AndroidX Media3 1.10.0. Use this skill to build a DefaultBandwidthMeter, share one BandwidthMeter between ExoPlayer and DefaultPreloadManager, tune TrackSelectionParameters bounds, detect network type via C.NetworkType, size DefaultLoadControl buffers for mobile, and avoid common low-bitrate ABR failure modes.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - bandwidthmeter
    - abr
    - trackselectionparameters
    - defaultloadcontrol
    - preloadmanager
    - networktype

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST NOT** set `TrackSelectionParameters.setMaxVideoBitrate` below the lowest variant in the ladder.
- Project **MUST NOT** share a `BandwidthMeter` singleton across unrelated processes.

## Step 1: plan

1. Enumerate every place that constructs `ExoPlayer.Builder`.
2. Identify the ABR ladder the backend serves.
3. Decide what "Auto" quality means in the UI.
4. Plan `DefaultLoadControl` buffer sizes for mobile.
5. Confirm that on "Wi-Fi only" mode, the app checks network type before starting playback.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
```

## Step 3: build a shared BandwidthMeter

### RIGHT

```kotlin
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter

object MediaSingletons {
    val bandwidthMeter: DefaultBandwidthMeter by lazy {
        DefaultBandwidthMeter.Builder(appContext)
            .setInitialBitrateEstimate(androidx.media3.common.C.NETWORK_TYPE_WIFI, 6_000_000L)
            .setInitialBitrateEstimate(androidx.media3.common.C.NETWORK_TYPE_4G,   3_000_000L)
            .setInitialBitrateEstimate(androidx.media3.common.C.NETWORK_TYPE_3G,     600_000L)
            .setInitialBitrateEstimate(androidx.media3.common.C.NETWORK_TYPE_2G,     120_000L)
            .setResetOnNetworkTypeChange(true)
            .build()
    }
}

val player = ExoPlayer.Builder(context)
    .setBandwidthMeter(MediaSingletons.bandwidthMeter)
    .build()
```

## Step 4: share the BandwidthMeter with PreloadManager

```kotlin
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager

val preloadManager = DefaultPreloadManager.Builder(MediaSingletons.bandwidthMeter)
    .setContext(context)
    .build()
```

## Step 5: bound TrackSelectionParameters

### RIGHT

```kotlin
import androidx.media3.common.TrackSelectionParameters

fun mobileDefaults(context: Context): TrackSelectionParameters =
    TrackSelectionParameters.Builder(context)
        .setMaxVideoSize(1920, 1080)
        .setMaxVideoBitrate(6_000_000)
        .setPreferredAudioLanguage("en")
        .setForceHighestSupportedBitrate(false)
        .build()

player.trackSelectionParameters = mobileDefaults(context)
```

### WRONG

```kotlin
// WRONG: 100 kbps cap resolves zero variants on almost every adaptive stream
player.trackSelectionParameters = TrackSelectionParameters.Builder(context)
    .setMaxVideoBitrate(100_000)
    .build()
```

## Step 6: detect network type

```kotlin
import androidx.media3.common.C
import androidx.media3.common.util.NetworkTypeObserver

fun currentMediaNetworkType(context: Context): Int =
    NetworkTypeObserver.getInstance(context).networkType
```

## Step 7: tune DefaultLoadControl for mobile

### RIGHT

```kotlin
import androidx.media3.exoplayer.DefaultLoadControl

val loadControl = DefaultLoadControl.Builder()
    .setBufferDurationsMs(
        /* minBufferMs = */ 30_000,
        /* maxBufferMs = */ 60_000,
        /* bufferForPlaybackMs = */ 2_500,
        /* bufferForPlaybackAfterRebufferMs = */ 5_000,
    )
    .setPrioritizeTimeOverSizeThresholds(true)
    .build()

val player = ExoPlayer.Builder(context)
    .setLoadControl(loadControl)
    .setBandwidthMeter(MediaSingletons.bandwidthMeter)
    .build()
```

**DO NOT** set `maxBufferMs` above 5 minutes on a mobile app. It starves memory and trips the 1.10.0 PreloadManager memory guard.

## Step 8: log ABR switches

```kotlin
import androidx.media3.common.Format
import androidx.media3.exoplayer.analytics.AnalyticsListener

val listener = object : AnalyticsListener {
    override fun onVideoInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: androidx.media3.exoplayer.DecoderReuseEvaluation?,
    ) {
        telemetry.trackAbrSwitch(format.width, format.height, format.bitrate)
    }
}
player.addAnalyticsListener(listener)
```

## Step 9: low-bitrate failure modes

- Set a realistic `initialBitrateEstimate` per network type.
- On network type change, `setResetOnNetworkTypeChange(true)` is sufficient.
- For prolonged stalls, combine with `StuckPlayerException` handling.

## Common pitfalls

- **One `BandwidthMeter` per activity.**
- **Separate `BandwidthMeter` for `PreloadManager`.**
- **Too-low `setMaxVideoBitrate`.**
- **Pinned "Auto" quality with a high bitrate cap on cellular.**
- **Huge `maxBufferMs`.**
- **Not checking `C.NetworkType` before playback on "Wi-Fi only" mode.**
- **Ignoring the initial bitrate estimate.**
- **Using a custom `TrackSelector` instead of tuning `TrackSelectionParameters`.**
