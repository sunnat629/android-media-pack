---
name: media3-analytics-telemetry
description: "Compact skill for Media3 QoE telemetry: TTFF, rebuffer, dropped frames, ABR, preload hit/miss, and player errors."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - analytics
    - telemetry
    - analyticslistener
    - playbackstats
    - ttff
    - dropped-frames
---

## Trigger

Use for measuring playback performance, scroll/reels handoff, startup delay, buffering, decode errors, and regression logs.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Register analytics before `prepare()`.
- Track media identity with every event.
- Measure prepare-to-first-frame and visible-active-to-first-frame.
- Separate content errors, decoder errors, network errors, and user abandons.

## Example

```kotlin
exo.addAnalyticsListener(statsListener)
exo.addListener(firstFrameAndErrorListener)
```

## Related

- `media3-bandwidth-abr`
- `streaming-media-architecture`
- `media3-video-playback`
