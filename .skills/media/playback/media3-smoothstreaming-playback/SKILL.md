---
name: media3-smoothstreaming-playback
description: "Compact skill for Media3 SmoothStreaming playback, manifest handling, fallback decisions, and adaptive stream caveats."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - smoothstreaming
    - adaptive
    - manifest
    - exoplayer
---

## Trigger

Use when an app must play SmoothStreaming manifests or add `media3-exoplayer-smoothstreaming`.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep SmoothStreaming code in Android source sets.
- Use this only when the stream source is SmoothStreaming; prefer HLS/DASH skills for modern adaptive delivery.
- Validate manifest URI, DRM needs, subtitles, and live/VOD behavior before changing UI.
- Track manifest parse errors separately from HTTP, DRM, codec, and renderer failures.
- Reuse shared ABR, load-control, and network policy from existing playback infrastructure.
- **MUST** set `MimeTypes.APPLICATION_SS` on the `MediaItem` when the URI does not end in `.ism/Manifest`; otherwise Media3 cannot infer the stream type.

## Example

```toml
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-exoplayer-smoothstreaming = { module = "androidx.media3:media3-exoplayer-smoothstreaming", version.ref = "media3" }
```

```kotlin
// URI ends in .ism/Manifest: type is inferred.
player.setMediaItem(MediaItem.fromUri(smoothStreamingManifestUri))
player.prepare()

// URI does not end in .ism/Manifest: set the MIME type explicitly.
player.setMediaItem(
    MediaItem.Builder()
        .setUri(opaqueManifestUri)
        .setMimeType(MimeTypes.APPLICATION_SS)
        .build()
)
player.prepare()
```

## Do Not

- Do not convert HLS/DASH tasks into SmoothStreaming work.
- Do not hide manifest failures behind a generic "video failed" message.
- Do not duplicate ABR policy already covered by `media3-bandwidth-abr`.

## Related

- `media3-hls-dash-adaptive-streaming`
- `media3-bandwidth-abr`
- `media3-drm-widevine-setup`
