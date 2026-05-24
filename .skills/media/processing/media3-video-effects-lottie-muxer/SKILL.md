---
name: media3-video-effects-lottie-muxer
description: "Compact skill for Media3 video effects, Lottie effects, muxer/export decisions, and Transformer-adjacent processing boundaries."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - effect
    - lottie
    - muxer
    - export
---

## Trigger

Use for `media3-effect`, `media3-effect-lottie`, `media3-muxer`, visual processing, overlays, effect export, or muxed output.

## Rules

- Start with `media3-transformer-editing` when the task includes export.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep effects/export code in Android source sets.
- Separate preview playback from final export; they can have different performance and fidelity constraints.
- Validate input/output container, audio preservation, frame rate, color, and device codec limits.
- Keep Lottie asset loading deterministic and bundled or cached.
- Track effect pipeline failures separately from playback failures.

## Example

```toml
media3-effect = { module = "androidx.media3:media3-effect", version.ref = "media3" }
media3-effect-lottie = { module = "androidx.media3:media3-effect-lottie", version.ref = "media3" }
media3-muxer = { module = "androidx.media3:media3-muxer", version.ref = "media3" }
```

## Do Not

- Do not apply expensive export effects directly inside Compose state updates.
- Do not assume preview output exactly matches final muxed output without validation.
- Do not mix processing errors into generic player analytics.

## Related

- `media3-transformer-editing`
- `media3-video-playback`
- `media3-inspector-metadata-thumbnails`
