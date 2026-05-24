---
name: migrate-exoplayer-to-media3
description: "Compact skill for migrating legacy ExoPlayer 2.x packages to AndroidX Media3 1.10.1."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - migration
    - exoplayer2
    - mediasession
    - playerview
    - cast
    - drm
---

## Trigger

Use when code imports `com.google.android.exoplayer2.*` or legacy session/notification APIs.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Do not mix `com.google.android.exoplayer2.*` and `androidx.media3.*` after migration.
- Replace `SimpleExoPlayer` with `ExoPlayer`.
- Replace legacy session/notification APIs with Media3 session/service.
- Run dependency tree checks for duplicate classes.

## Example

```text
com.google.android.exoplayer2.Player -> androidx.media3.common.Player
com.google.android.exoplayer2.ExoPlayer -> androidx.media3.exoplayer.ExoPlayer
```

## Related

- `media3-background-playback-service`
- `media3-compose-ui-material3`
- `media3-datasources-networking`
