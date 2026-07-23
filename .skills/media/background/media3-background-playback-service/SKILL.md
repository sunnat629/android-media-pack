---
name: media3-background-playback-service
description: "Compact skill for Media3 MediaSessionService, MediaController, notification, media buttons, and service lifetime."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.2"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
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
- You **MUST** declare both `android.permission.FOREGROUND_SERVICE` and `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK`; both are mandatory on targetSdk 34+.
- The service manifest entry **MUST** include the `androidx.media3.session.MediaSessionService` intent-filter action; add `android.media.browse.MediaBrowserService` so legacy browser clients (for example Android Auto) can bind.
- Do not use legacy `MediaSessionCompat`, `MediaSessionConnector`, or `PlayerNotificationManager`.

## Example

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<service android:name=".playback.PlaybackService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>
```

## Related

- `media3-lifecycle-state`
- `media3-audio-playback`
- `media3-video-playback`
