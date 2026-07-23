---
name: media3-android-auto-media-surface
description: "Compact skill for Android Auto media playback surfaces using Media3 sessions, browsable media, transport controls, and safety constraints."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - android-auto
    - mediasession
    - media-library
    - car
---

## Trigger

Use for Android Auto media playback, car-compatible browse trees, transport controls, or Media3 session/service integration for car surfaces.

## Rules

- Start with `media3-background-playback-service`; Auto depends on a correct session/service boundary.
- Pin Media3 to `1.10.1` through the version catalog.
- Prefer Media3 session and controller APIs; avoid legacy session compat unless the host app already requires it.
- Expose browsable media, stable IDs, metadata, artwork, and transport actions through the media session layer.
- Respect Auto UX restrictions: no custom phone-style playback screen, unsafe gestures, or dense interaction.
- Keep auth, entitlement, and network failures visible through safe playback/browser state.
- Verify current Android Auto media app requirements before release.

## Example

```toml
media3-session = { module = "androidx.media3:media3-session", version.ref = "media3" }
```

```xml
<service
    android:name=".playback.PlaybackService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>

<meta-data
    android:name="com.google.android.gms.car.application"
    android:resource="@xml/automotive_app_desc" />
```

The intent-filter actions let Auto discover and bind the service for both Media3 and legacy browser clients. The `automotive_app_desc` meta-data (an XML resource declaring `<uses name="media"/>`) marks the app as an Auto media app; without it Auto will not surface the app.

## Do Not

- Do not build a custom in-car UI as if Auto were a tablet.
- Do not let Activity lifecycle own car playback.
- Do not expose unstable media IDs in the browse tree.

## Related

- `media3-background-playback-service`
- `media3-audio-playback`
- `media3-lifecycle-state`
