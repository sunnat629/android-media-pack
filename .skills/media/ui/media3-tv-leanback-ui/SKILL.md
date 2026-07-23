---
name: media3-tv-leanback-ui
description: "Compact skill for Android TV Media3 playback UI. New TV apps use Compose for TV; Leanback and media3-ui-leanback are maintenance-only for existing Leanback apps. Covers D-pad focus, overscan-safe controls, and TV UX rules."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - tv
    - compose-for-tv
    - leanback
    - dpad
    - player-ui
---

## Trigger

Use for Android TV playback screens, remote/D-pad interaction, Compose for TV player UI, or maintenance of an existing Leanback app that uses `media3-ui-leanback`.

## Rules

- Start with `streaming-media-architecture` for ownership and telemetry, then specialize for TV.
- Pin Media3 to `1.10.1` through the version catalog.
- `androidx.leanback` is formally deprecated. Its release page states the artifact and its classes are deprecated and points to Compose for TV. `media3-ui-leanback` wraps that deprecated stack.
- New TV apps **MUST** use Compose for TV (`androidx.tv:tv-material`) with the standard Media3 Compose surface (`PlayerSurface`/`ContentFrame` from `media3-ui-compose`).
- Use Leanback plus `media3-ui-leanback` (`LeanbackPlayerAdapter`) only when maintaining an existing Leanback app. **DO NOT** add it to new code.
- When an existing Leanback app is being modernized, follow the official migration guide: https://developer.android.com/training/tv/playback/leanback/migrate-to-compose
- On both stacks, design for D-pad focus, visible selected state, back/menu behavior, and transport keys.
- Keep video controls readable at TV distance and safe from overscan/system UI.
- Use TV-friendly browse/playback flows; avoid phone gestures as required controls.
- Test focus order before claiming the TV UI is done.

## Example

New TV app (Compose for TV):

```toml
tv-material = { module = "androidx.tv:tv-material", version.ref = "tvMaterial" }
media3-ui-compose = { module = "androidx.media3:media3-ui-compose", version.ref = "media3" }
```

Existing Leanback app, maintenance only:

```toml
media3-ui-leanback = { module = "androidx.media3:media3-ui-leanback", version.ref = "media3" }
```

## Do Not

- **DO NOT** start a new TV app on Leanback or `media3-ui-leanback`.
- **DO NOT** ship a TV UI that requires touch.
- **DO NOT** hide focus state.
- **DO NOT** reuse cramped phone controls without TV spacing and focus checks.

## Related

- `media3-compose-ui-material3`
- `media3-view-ui-player`
- `media3-video-playback`
- `media3-background-playback-service`
