# ABR tuning: TrackSelectionParameters, BandwidthMeter, LoadControl

## TrackSelectionParameters presets

Cap the ladder with `setMaxVideoSize` and `setMaxVideoBitrate`. Use the device class to pick sensible bounds rather than hardcoding a single ceiling.

```kotlin
import androidx.media3.common.TrackSelectionParameters

enum class DeviceTier { PHONE_LOW, PHONE_MID, PHONE_HIGH, TABLET, TV }

fun paramsFor(tier: DeviceTier, context: android.content.Context): TrackSelectionParameters =
    TrackSelectionParameters.Builder(context).apply {
        when (tier) {
            DeviceTier.PHONE_LOW  -> { setMaxVideoSize(854, 480);   setMaxVideoBitrate(1_200_000) }
            DeviceTier.PHONE_MID  -> { setMaxVideoSize(1280, 720);  setMaxVideoBitrate(2_800_000) }
            DeviceTier.PHONE_HIGH -> { setMaxVideoSize(1920, 1080); setMaxVideoBitrate(6_000_000) }
            DeviceTier.TABLET     -> { setMaxVideoSize(1920, 1080); setMaxVideoBitrate(6_000_000) }
            DeviceTier.TV         -> { setMaxVideoSize(3840, 2160); setMaxVideoBitrate(18_000_000) }
        }
        setPreferredAudioLanguage("en")
    }.build()
```

**DO NOT** set `setMaxVideoBitrate` below the lowest variant in the manifest. The selector resolves to zero tracks and playback fails.

## DefaultBandwidthMeter overrides

**PREFERRED** default: `DefaultBandwidthMeter.getSingletonInstance(context)`. Override only with measured evidence.

```kotlin
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter

val meter = DefaultBandwidthMeter.Builder(context)
    .setInitialBitrateEstimate(2_500_000L)
    .setSlidingWindowMaxWeight(4_000)
    .build()
```

- `setInitialBitrateEstimate` helps cold-start in known-poor-connectivity regions.
- `setSlidingWindowMaxWeight` trades responsiveness for stability. Raise on noisy cellular.

## LoadControl buffering windows

| Profile | minBufferMs | maxBufferMs | bufferForPlaybackMs | bufferForPlaybackAfterRebufferMs |
| --- | --- | --- | --- | --- |
| Mobile default | 30_000 | 60_000 | 2_500 | 5_000 |
| Live | 15_000 | 30_000 | 1_500 | 3_000 |
| TV / stable Wi-Fi | 60_000 | 120_000 | 2_500 | 5_000 |

**DO NOT** exceed `maxBufferMs = 300_000` on mobile. It starves heap and trips the 1.10.0 PreloadManager memory guard.

## Rules

- **MUST** let Media3 pick variants. Hand-rolled `TrackSelectionOverride` breaks on manifest updates.
- **MUST** include the `context` when building `TrackSelectionParameters`. The context-aware overload honors display capability.
- **PREFERRED** is tuning caps per device tier over per-user.
- **DO NOT** lock the player to a single variant unless product policy requires it. Document the override if you do.
