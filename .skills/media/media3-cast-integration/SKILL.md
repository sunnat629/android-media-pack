---
name: media3-cast-integration
description: "Compact skill for Media3 CastPlayer, local-to-remote handoff, MediaRouteButton, and Cast session lifecycle."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - cast
    - chromecast
    - castplayer
    - mediaroutebutton
    - handoff
---

## Trigger

Use for Chromecast/Cast support and local player to remote receiver handoff.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Use `CastPlayer` and local player integration; avoid hand-swapping UI players.
- Declare CastOptionsProvider.
- Confirm receiver supports stream MIME/types.
- Keep session state and progress consistent across handoff.

## Example

```toml
media3-cast = { module = "androidx.media3:media3-cast", version.ref = "media3" }
```

## Related

- `media3-background-playback-service`
- `media3-video-playback`
