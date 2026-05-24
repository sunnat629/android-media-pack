---
name: media3-inspector-metadata-thumbnails
description: "Compact skill for Media3 inspector metadata, thumbnail/frame extraction, and container sample inspection without playback."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - inspector
    - metadata
    - thumbnail
    - frameextractor
    - mediaextractor
---

## Trigger

Use for metadata reads, thumbnails, frame extraction, and media inspection that should not instantiate ExoPlayer.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Run extraction off main thread.
- Prefer server thumbnails or manifest thumbnail tracks for feeds.
- Close extractor resources.
- Do not decode frames during scroll unless cached and bounded.

## Example

```toml
media3-inspector = { module = "androidx.media3:media3-inspector", version.ref = "media3" }
```

## Related

- `media3-vod-playback`
- `media3-video-playback`
