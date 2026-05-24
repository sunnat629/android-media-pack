---
name: streaming-media-architecture
description: "Compact top-level skill for production Android/KMP streaming architecture with Media3 1.10.1, Compose, repositories, offline-first data, and reels/feed playback."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - kmp
    - media3
    - compose
    - reels
    - feed
    - preload
    - offline-first
    - telemetry
---

## Trigger

Use first for Media3 architecture, reels/short-video feeds, KMP split decisions, player ownership, preloading, and production streaming readiness.

## Rules

- Baseline: Media3 `1.10.1`; use version catalog, no hardcoded Gradle versions.
- KMP/common: domain models, repositories, playback policy, feed ranking, state, events, analytics contracts.
- Android source set: ExoPlayer, MediaSessionService, MediaController, Media3 UI, DataSource, cache, DRM.
- UI is UDF: immutable state down, events up. Repositories own truth; UI never calls network directly.
- One playback owner per screen/session. Do not create/release a player per feed row.
- Short feed uses bounded sliding window. Preload next/prev only; respect data saver, battery, thermal, memory.
- Telemetry comes from `AnalyticsListener`, `PlaybackStatsListener`, player callbacks, and measured UI handoff points.

## Short-Video Feed Pattern

- A playback service or screen-scoped owner manages one Android ExoPlayer and optional MediaSession.
- Common/KMP-safe controller APIs build a video-only media queue from feed items.
- Active page is guarded by stable media identity; database id is best, URI is acceptable fallback.
- State contract includes `mediaUri` and `renderedMediaUri`.
- Compose attaches the player surface as soon as the active item owns the player.
- Keep thumbnail above the player until `onRenderedFirstFrame` reports the same media identity.
- Remove thumbnail only when `activeItem == mediaUri == renderedMediaUri`.
- Keep `ContentFrame`/player surface in the composition. If it is hidden until ready, first-frame callback may never arrive.
- Inline scrollers can use `TextureView` for overlay/thumbnail handoff. Prefer `SurfaceView` for stable fullscreen/HDR surfaces.
- Use `setPauseAtEndOfMediaItems(true)` so pager navigation controls item advance.
- Use small `ExoPlayer.PreloadConfiguration`, then graduate to `DefaultPreloadManager` when queue/window telemetry proves it is needed.

## Short-Feed Policy

```text
current: play
next: preload 2-5s
previous: metadata or 1-2s
beyond window: no preload
data/battery/thermal pressure: metadata only or off
```

## Example

```kotlin
data class PlayerState(
    val mediaUri: String? = null,
    val renderedMediaUri: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

val showThumbnail = activeVideoUri != null &&
    (playerState.mediaUri != activeVideoUri ||
        playerState.renderedMediaUri != activeVideoUri)
```

## Related

- `media3-video-playback`: surface, aspect, first frame, PiP, HDR.
- `media3-compose-ui-material3`: Compose player UI and controls.
- `media3-hls-dash-adaptive-streaming`: adaptive streams and manifests.
- `media3-datasources-networking`: HTTP, cache, custom DataSource.
- `media3-bandwidth-abr`: ABR, bandwidth, load control.
- `media3-analytics-telemetry`: QoE, TTFF, dropped frames, rebuffer.
- `media3-background-playback-service`: MediaSessionService.
- `migrate-exoplayer-to-media3`: legacy ExoPlayer migration.

## Do Not

- Do not put Media3 APIs in `commonMain`.
- Do not hide player failure behind generic network errors.
- Do not preload unlimited items to chase instant playback.
- Do not use custom DataSource until telemetry shows real URI-open, cache, auth, transform, or retry needs.
- Do not copy Netflix/TikTok/Meta scale architecture blindly; copy bounded preload, telemetry, and device-aware delivery principles.

## Sources

- Android Media3 releases: https://developer.android.com/jetpack/androidx/releases/media3
- Media3 Compose UI: https://developer.android.com/media/media3/ui/compose
- Media3 PreloadManager: https://developer.android.com/media/media3/exoplayer/preloading-media/preloadmanager
- Preload concepts: https://developer.android.com/media/media3/exoplayer/preloading-media/preloadmanager/concepts
- Android app architecture: https://developer.android.com/topic/architecture
- Android offline-first: https://developer.android.com/topic/architecture/data-layer/offline-first
- Android KMP: https://developer.android.com/kotlin/multiplatform
- AndroidX Media: https://github.com/androidx/media
- Now in Android: https://github.com/android/nowinandroid
- Jetcaster: https://github.com/android/compose-samples/tree/main/Jetcaster
