---
name: media3-live-streaming
description: "Compact skill for Media3 live streams, live offset, DVR window, catch-up, reconnect, and behind-live-window recovery."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - live
    - hls
    - dash
    - dvr
    - live-offset
    - reconnect
---

## Trigger

Use for live playback, DVR scrubbing, live edge behavior, reconnect states, latency tuning, and live stream errors.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Configure live offset on `MediaItem.LiveConfiguration`.
- Allow small playback speed adjustments for latency recovery.
- Handle `ERROR_CODE_BEHIND_LIVE_WINDOW` explicitly.
- UI state must distinguish connecting, live, stalled, reconnecting, ended.

## Example

```kotlin
MediaItem.LiveConfiguration.Builder()
    .setTargetOffsetMs(6_000)
    .setMinPlaybackSpeed(0.97f)
    .setMaxPlaybackSpeed(1.03f)
    .build()
```

## Related

- `media3-live-only-streaming`
- `media3-hls-dash-adaptive-streaming`
- `media3-analytics-telemetry`
