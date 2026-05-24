---
name: media3-background-playback-service
description: "Compact skill for Media3 MediaSessionService, MediaController, notification, media buttons, and service lifetime."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - mediasessionservice
    - mediasession
    - mediacontroller
    - foreground-service
    - notification
---

## Trigger

Use when playback must survive Activity recreation/backgrounding or expose system notification, media buttons, external controllers.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Exactly one service-owned player for background/session playback.
- UI talks through `MediaController`; UI does not own the service player.
- Declare media playback foreground service permissions.
- Do not use legacy `MediaSessionCompat`, `MediaSessionConnector`, or `PlayerNotificationManager`.

## Example

```xml
<service android:name=".playback.PlaybackService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback" />
```

## Related

- `media3-lifecycle-state`
- `media3-audio-playback`
- `media3-video-playback`
