---
name: media3-hls-dash-adaptive-streaming
description: "Compact skill for Media3 HLS/DASH adaptive streaming, manifests, subtitles, live-vs-VOD behavior, and buffer policy."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - hls
    - dash
    - adaptive
    - manifest
    - subtitles
    - live
    - vod
---

## Trigger

Use for HLS/DASH source setup, ABR manifest issues, subtitles, variant selection, live/DVR behavior, or stream compatibility.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Set MIME type when URI extension is unreliable.
- Keep manifest fixes server-side when possible.
- Handle subtitles through track selection parameters.
- Do not mix live and VOD semantics in one timeline.

## Example

```kotlin
MediaItem.Builder()
    .setUri(url)
    .setMimeType(MimeTypes.APPLICATION_M3U8)
    .build()
```

## Related

- `media3-bandwidth-abr`
- `media3-live-streaming`
- `media3-vod-playback`
- [references/abr-tuning.md](references/abr-tuning.md) for ABR and buffer tuning detail
- [references/manifest-errors.md](references/manifest-errors.md) for manifest failure classification and recovery
