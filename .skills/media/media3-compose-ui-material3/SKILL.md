---
name: media3-compose-ui-material3
description: "Compact skill for Media3 Compose UI with ContentFrame, PlayerSurface, controls, lifecycle-safe state, and Material3."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - compose
    - material3
    - contentframe
    - playersurface
    - controls
    - mediacontroller
---

## Trigger

Use for Compose player surfaces, controls, timers, overlays, insets, state collection, and replacing XML PlayerView UI.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Prefer `media3-ui-compose` / Material3 components for new Compose screens.
- Keep `@UnstableApi` opt-ins narrow.
- Player surface size must be stable; controls must not resize the video.
- Keep controls state derived from `Player` callbacks or controller state.

## Example

```toml
media3-ui-compose = { module = "androidx.media3:media3-ui-compose", version.ref = "media3" }
media3-ui-compose-material3 = { module = "androidx.media3:media3-ui-compose-material3", version.ref = "media3" }
```

## Related

- `media3-video-playback`
- `media3-lifecycle-state`
- `migrate-xml-ui-to-compose`
