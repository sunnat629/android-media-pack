---
name: media3-video-playback
description: "Compact skill for Media3 1.10.1 video surfaces, aspect ratio, first frame, HDR, PiP, and feed-safe handoff."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.2"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - video
    - surface
    - playersurface
    - contentframe
    - first-frame
    - hdr
    - pip
---

## Trigger

Use for video rendering bugs, blink/flicker, wrong crop, first-frame handoff, PiP, HDR, or fullscreen behavior.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Use `onRenderedFirstFrame` plus media identity to remove placeholders.
- Do not change surface type while the player is attached.
- `SurfaceView` is **PREFERRED** everywhere, including inline scrollers. Fall back to `TextureView` only when `SurfaceView` cannot satisfy the requirement, for example smooth scroll or transition animation on pre-API 24 devices. `TextureView` costs more power, has weaker frame timing, and cannot show HDR or secure (DRM) output.
- Aspect comes from `onVideoSizeChanged`, not thumbnail size.

## Example

```kotlin
val readyForVideo = state.mediaUri == uri && state.renderedMediaUri == uri
// thumbnail visible while !readyForVideo
```

## Related

- `media3-compose-ui-material3`
- `media3-analytics-telemetry`
- `streaming-media-architecture`
