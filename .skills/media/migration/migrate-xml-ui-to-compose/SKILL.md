---
name: migrate-xml-ui-to-compose
description: "Compact skill for migrating XML PlayerView media UI to Media3 Compose UI."
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
    - migration
    - playerview
    - playersurface
    - xml
---

## Trigger

Use when replacing XML `PlayerView`/custom controls with Compose Media3 surfaces and controls.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Migrate one screen at a time.
- Do not attach XML `PlayerView` and Compose surface to the same player simultaneously.
- Translate controls to Compose slots/state.
- Keep fullscreen/subtitle/settings behavior covered.

## Example

```text
PlayerView XML -> ContentFrame/PlayerSurface + Compose controls
```

## Related

- `media3-compose-ui-material3`
- `media3-video-playback`
- `media3-lifecycle-state`
