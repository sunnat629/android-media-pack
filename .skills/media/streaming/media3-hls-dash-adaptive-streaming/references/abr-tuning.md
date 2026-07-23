# ABR tuning: TrackSelectionParameters, BandwidthMeter, LoadControl

## TrackSelectionParameters presets

Cap the ladder with `setMaxVideoSize` and `setMaxVideoBitrate`. Use the device class to pick sensible bounds rather than hardcoding a single ceiling.

```kotlin
import androidx.media3.common.TrackSelectionParameters

enum class DeviceTier { PHONE_LOW, PHONE_MID, PHONE_HIGH, TABLET, TV }

fun paramsFor(tier: DeviceTier): TrackSelectionParameters =
    TrackSelectionParameters.Builder().apply {
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

**DO NOT** set `setMaxVideoBitrate` below the lowest variant in the manifest. With the `DefaultTrackSelector` defaults (`exceedVideoConstraintsIfNecessary = true`) the constraints are exceeded and the lowest variant still plays, so the cap silently does nothing. Selection resolves to zero tracks and playback fails only if that flag was disabled.

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

**DO NOT** exceed `maxBufferMs = 300_000` on mobile. Oversized buffers pressure the heap. To bound preload memory, use the opt-in `DefaultLoadControl.Builder.setPlayerTargetBufferBytes` (added in Media3 1.9.0) together with `DefaultPreloadManager.Builder.setLoadControl`.

## Rules

- **MUST** let Media3 pick variants. Hand-rolled `TrackSelectionOverride` breaks on manifest updates.
- **MUST** use the no-arg `TrackSelectionParameters.Builder()`. The `Builder(Context)` overload is deprecated in 1.10.1 and the context argument is ignored.
- **PREFERRED** is tuning caps per device tier over per-user.
- **DO NOT** lock the player to a single variant unless product policy requires it. Document the override if you do.
