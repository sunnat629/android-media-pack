---
name: media3-ads-ima
description: "Compact skill for Media3 Google IMA CSAI/SSAI ad insertion, AdViewProvider, companions, and ad telemetry."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.2"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - ima
    - ads
    - csai
    - ssai
    - dai
    - adviewprovider
---

## Trigger

Use for IMA ads, CSAI, SSAI/DAI, ad view placement, companion ads, and ad/content error separation.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Choose CSAI or SSAI per playback session.
- Ad UI must be provided by an `AdViewProvider` attached to the playback surface.
- Separate ad telemetry from content telemetry but correlate by session.
- A playlist **MUST NOT** contain more than one IMA SSAI stream. One SSAI stream may be combined with other non-SSAI media items.

## Example

```toml
media3-exoplayer-ima = { module = "androidx.media3:media3-exoplayer-ima", version.ref = "media3" }
```

## Related

- `media3-video-playback`
- `media3-analytics-telemetry`
