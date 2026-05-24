---
name: media3-rtsp-playback
description: "Compact skill for Media3 RTSP playback with camera feeds, network stream caveats, buffering, and Android source-set boundaries."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - rtsp
    - camera
    - playback
    - exoplayer
---

## Trigger

Use for RTSP camera feeds, surveillance streams, LAN streams, or apps adding `media3-exoplayer-rtsp`.

## Rules

- Start with `streaming-media-architecture` for player ownership, KMP split, preload, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep RTSP playback in Android source sets; expose KMP-safe state/events upward.
- Use RTSP only for `rtsp://` streams; use HLS/DASH/HTTP skills for normal CDN playback.
- Treat network reachability, NAT, firewalls, and camera auth as first-class failure causes.
- Keep timeout, retry, and reconnect policy outside UI composables.
- Log transport, URI host, player error code, and retry count without leaking credentials.

## Example

```toml
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-exoplayer-rtsp = { module = "androidx.media3:media3-exoplayer-rtsp", version.ref = "media3" }
```

```kotlin
player.setMediaItem(MediaItem.fromUri(rtspUri))
player.prepare()
```

## Do Not

- Do not put usernames, passwords, or camera tokens in logs.
- Do not create one player per camera thumbnail without a bounded policy.
- Do not treat RTSP failures as generic internet failures.

## Related

- `media3-video-playback`
- `media3-datasources-networking`
- `media3-analytics-telemetry`
