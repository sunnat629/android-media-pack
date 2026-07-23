---
name: media3-cast-integration
description: "Compact skill for Media3 CastPlayer.Builder, automatic local/remote handoff, MediaRouteButton, and Cast session lifecycle."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.2"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - cast
    - chromecast
    - castplayer
    - remotecastplayer
    - mediaroutebutton
    - handoff
---

## Trigger

Use for Chromecast/Cast support and local player to remote receiver handoff.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- All `CastPlayer` constructors are deprecated since Media3 1.9.0. You **MUST** build with `CastPlayer.Builder` and `.setLocalPlayer(exoPlayer)` for combined local plus remote playback, or with `RemoteCastPlayer.Builder` for remote-only control.
- A `CastPlayer` built with `setLocalPlayer` handles local/remote transitions automatically when a Cast session starts or ends. **DO NOT** hand-swap players in UI or session code.
- Wrap that single `CastPlayer` in exactly one `MediaSession` inside your `MediaSessionService`; controllers, notification, and media buttons then follow handoff without extra code.
- Declare the options provider through manifest meta-data `com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME` pointing to `androidx.media3.cast.DefaultCastOptionsProvider` or a custom `OptionsProvider`.
- Add the `androidx.mediarouter` `MediaTransferReceiver` so the system Output Switcher can move playback between local and remote devices.
- Confirm the receiver supports the stream MIME types before handoff.
- Keep session state and progress consistent across handoff.

## Example

```toml
media3-cast = { module = "androidx.media3:media3-cast", version.ref = "media3" }
```

```kotlin
val castPlayer = CastPlayer.Builder(context)
    .setLocalPlayer(exoPlayer)
    .build()
val mediaSession = MediaSession.Builder(this, castPlayer).build()
```

```xml
<meta-data
    android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
    android:value="androidx.media3.cast.DefaultCastOptionsProvider" />
```

## Related

- `media3-background-playback-service`
- `media3-video-playback`
