---
name: media3-tv-leanback-ui
description: "Compact skill for Android TV Media3 playback UI with Leanback, D-pad focus, overscan-safe controls, and TV UX rules."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - tv
    - leanback
    - dpad
    - player-ui
---

## Trigger

Use for Android TV playback screens, Leanback controls, remote/D-pad interaction, and `media3-ui-leanback`.

## Rules

- Start with `streaming-media-architecture` for ownership and telemetry, then specialize for TV.
- Pin Media3 to `1.10.1` through the version catalog.
- Add `media3-ui-leanback` only for Android TV/Leanback surfaces.
- Design for D-pad focus, visible selected state, back/menu behavior, and transport keys.
- Keep video controls readable at TV distance and safe from overscan/system UI.
- Use TV-friendly browse/playback flows; avoid phone gestures as required controls.
- Test focus order before claiming the TV UI is done.

## Example

```toml
media3-ui = { module = "androidx.media3:media3-ui", version.ref = "media3" }
media3-ui-leanback = { module = "androidx.media3:media3-ui-leanback", version.ref = "media3" }
```

## Do Not

- Do not ship a TV UI that requires touch.
- Do not hide focus state.
- Do not reuse cramped phone controls without TV spacing and focus checks.

## Related

- `media3-view-ui-player`
- `media3-video-playback`
- `media3-background-playback-service`
