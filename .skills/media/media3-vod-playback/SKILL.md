---
name: media3-vod-playback
description: "Compact skill for Media3 VOD items, resume position, playlists, chapters, thumbnails, and next-item preload."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - vod
    - resume
    - playlist
    - chapters
    - thumbnails
    - preload
---

## Trigger

Use for on-demand video playback, continue-watching, static playlists, chapters, scrubber thumbnails, and next episode behavior.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Stable content id keys resume progress.
- Seek to resume only after player is ready enough to accept it.
- Autoadvance is product policy, not a player default.
- Do not synthesize thumbnails on-device unless explicitly required.

## Example

```kotlin
MediaItem.Builder()
    .setMediaId(videoId)
    .setUri(streamUri)
    .build()
```

## Related

- `media3-hls-dash-adaptive-streaming`
- `media3-inspector-metadata-thumbnails`
- `media3-background-playback-service`
