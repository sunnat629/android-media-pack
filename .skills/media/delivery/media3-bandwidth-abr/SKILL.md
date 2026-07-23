---
name: media3-bandwidth-abr
description: "Compact skill for Media3 bandwidth estimation, ABR limits, load control, network constraints, and preload contention."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.2"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - abr
    - bandwidthmeter
    - trackselection
    - loadcontrol
    - preload
---

## Trigger

Use for bitrate quality, startup stalls, low-quality playback, network-aware limits, or preload starving active playback.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Share bandwidth knowledge between player/preload paths when possible.
- Use sensible mobile bitrate ceilings; never cap below the lowest playable variant.
- Shrink buffers/preload under data saver, battery saver, thermal pressure, or cellular constraints.
- Do not set huge mobile buffers to mask bad manifests.

## Example

```kotlin
TrackSelectionParameters.Builder()
    .setMaxVideoBitrate(6_000_000)
    .build()
```

The `Context` overload of `TrackSelectionParameters.Builder` is deprecated; you **MUST** use the no-argument builder.

## Related

- `media3-hls-dash-adaptive-streaming`
- `media3-datasources-networking`
- `media3-analytics-telemetry`
