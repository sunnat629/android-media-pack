---
name: media3-xr-media-surface
description: "Compact planning skill for Android XR media playback surfaces with current-doc verification, immersive layout, controller input, and fallback behavior."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - xr
    - immersive
    - large-screen
    - playback
---

## Trigger

Use for Android XR media playback planning, immersive playback surfaces, spatial/large display behavior, or controller/gesture input questions.

## Rules

- Verify current Android XR docs and available APIs before implementation; XR guidance can move faster than Media3.
- Start with `media3-adaptive-compose-ui` for size classes, then add XR-specific interaction.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 player/session code in Android source sets.
- Decide embedded vs immersive playback before coding controls.
- For spatial video, render through Jetpack XR SDK SceneCore `SpatialExternalSurface` (or its 180/360 degree variants) with a Media3 `ExoPlayer` attached to the provided surface.
- MV-HEVC spatial video playback requires Media3 1.6.0 or newer; the pinned `1.10.1` satisfies this.
- Make controls usable with controller, hand, gaze, or system input supported by the target XR stack.
- Provide a phone/tablet fallback path if XR APIs are unavailable.

## Example

```toml
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-ui-compose = { module = "androidx.media3:media3-ui-compose", version.ref = "media3" }
```

## Do Not

- Do not pretend XR is just a larger phone layout.
- Do not hard-code immersive behavior without a fallback.
- Do not ship stale XR API names without checking current official docs.

## Related

- `media3-adaptive-compose-ui`
- `media3-compose-ui-material3`
- `media3-video-playback`
