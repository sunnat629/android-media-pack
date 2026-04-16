# Media3 release history

This document tracks the AndroidX Media3 release history from the first stable version to the version currently pinned by this skill pack. Each skill in `.skills/media/*/SKILL.md` is pinned to a specific `metadata.target_media3_version` and reviewed against the release notes below.

Canonical source: https://developer.android.com/jetpack/androidx/releases/media3

Current pack target: **Media3 1.9.0** (December 2025)

---

## 1.0.0 — March 2023

First stable release of AndroidX Media3. Unified successor to the standalone `com.google.android.exoplayer2.*` library.

Highlights:
- New package namespace `androidx.media3.*`.
- `ExoPlayer` replaces `SimpleExoPlayer`.
- `MediaSession` in `androidx.media3.session` replaces `MediaSessionCompat` and `MediaSessionConnector`.
- `PlayerView` replaces `StyledPlayerView` and uses the styled layout by default.
- `MediaSessionService` generates playback notifications automatically, replacing `PlayerNotificationManager`.
- `DefaultMediaSourceFactory` accepts `MediaItem` instances that declare MIME type, DRM, and subtitles inline.
- Modules split by feature: `media3-exoplayer`, `-exoplayer-hls`, `-exoplayer-dash`, `-exoplayer-smoothstreaming`, `-exoplayer-rtsp`, `-ui`, `-session`, `-cast`, `-datasource-okhttp`, `-datasource-cronet`, `-transformer`.

## 1.1.0 — July 2023

Stabilization release.

Highlights:
- Improved HLS segment selection and playlist reload handling.
- Better handling of `MediaMetadata` provided via `MediaItem`.
- Transformer improvements toward stable editing APIs.

## 1.2.0 — November 2023

Highlights:
- Initial **PreloadManager** API (experimental) for preloading next items in feeds.
- DASH low-latency improvements.
- Transformer progresses toward production use cases.
- Improvements to `MediaSessionService` discovery and notification styling.

## 1.3.0 — March 2024

Highlights:
- `media3-effect` module with GL-backed `OverlayEffect`, `MatrixTransformation`, and `TextureBitmapReader`.
- Improvements to `MediaController` and `MediaBrowser` bridging.
- RTSP improvements for live low-latency scenarios.
- `HttpEngineDataSource.Factory` introduced to support Android 14 `HttpEngine` (Cronet-backed).

## 1.4.0 — July 2024

Highlights:
- HLS interstitials (`HlsInterstitialsAdsLoader`) for client-side HLS ad insertion.
- `HlsMediaSource` improvements for CMAF low-latency.
- Further Transformer stabilization.
- Bug fixes for adaptive track selection on low-bandwidth networks.

## 1.5.0 — December 2024

Highlights:
- Session API refinements: safer controller connection flows, richer command handling.
- Improved `CastPlayer` bridging with `MediaSession`.
- Wider DRM error surface through `PlaybackException.ERROR_CODE_DRM_*`.
- First set of helper state holders for Jetpack Compose (experimental).

## 1.6.0 — April 2025

Highlights:
- **`media3-ui-compose`** module introduced with state classes (`rememberPlayPauseButtonState`, `rememberPresentationState`) and composable surfaces.
- `PlayerSurface` composable for rendering video in Compose without wrapping `PlayerView` in `AndroidView`.
- Improvements to adaptive track selection for Compose-driven UIs.
- PreloadManager iteration: shared `BandwidthMeter` with the player for coordinated bandwidth usage.

## 1.7.0 — Mid 2025

Highlights:
- Stabilization of `media3-ui-compose` state holders.
- Incremental DASH and HLS parsing fixes.
- Ongoing Transformer improvements (faster speed adjustments, improved muxer paths).

## 1.8.0 — 30 July 2025

Highlights:
- Transformer: MP4 edit list trim (`experimentalSetMp4EditListTrimEnabled(true)`) for faster trim-only edits that avoid full re-encode.
- Media resumption notification improvements after device reboot.
- PreloadManager refinements toward production rollout.

## 1.9.0 — 19 December 2025 (current pack target)

Highlights that this skill pack is pinned to:
- **`media3-inspector`** module introduced:
  - `MetadataRetriever` replaces `android.media.MediaMetadataRetriever` for duration and format reads.
  - `FrameExtractor` for off-main-thread frame and thumbnail extraction.
  - `MediaExtractorCompat` replaces the platform `MediaExtractor` for sample-level inspection.
- **`media3-ui-compose-material3`** Material3 building blocks: `ContentFrame`, `PlayPauseButton`, `SeekBackButton`, `SeekForwardButton`.
- **`CastPlayer` rewrite**: `CastPlayer.Builder(context).setLocalPlayer(exoPlayer)` handles local-to-remote handoff automatically. Manual `Player` swapping is no longer required.
- **`setMediaButtonPreferences`** replaces `setCustomLayout` for standard `Player.COMMAND_*` values in the notification drawer.
- **`player.mute()` / `player.unmute()`**: native mute support, replacing manual volume caching.
- **`StuckPlayerException`**: surfaces hard stalls via `Player.Listener.onPlayerError`.
- **Automatic wake lock management**: Media3 holds the wake lock during playback by default. Manual `PowerManager.WakeLock` around playback is incorrect.
- **`selectTextByDefault`** on `TrackSelectionParameters` replaces hand-rolled subtitle overrides for the default-on case.
- **Transformer**: default muxer is `InAppMp4Muxer` rather than the platform `MediaMuxer`. `EditedMediaItemSequence` declares track types at creation time.
- **PreloadManager**: disk caching via `PreloadStatus.specifiedRangeCached`, automatic memory management with a default 144 MB upper bound on `DefaultLoadControl`.
- **`media3-decoder-av1`** module added for AV1 software decoding on devices without a hardware decoder.
- HLS interstitials fixes and CAN-BLOCK-RELOAD reload backoff.

---

## Pack version mapping

| Pack skill | Pinned `target_media3_version` | Committed |
|---|---|---|
| `migrate-exoplayer-to-media3` | 1.9.0 | 2026-04-16 |
| `media3-background-playback-service` | 1.9.0 | 2026-04-16 |
| `media3-drm-widevine-setup` | 1.9.0 | 2026-04-16 |
| `media3-compose-ui-material3` | 1.9.0 | 2026-04-16 |
| `media3-hls-dash-adaptive-streaming` | 1.9.0 | 2026-04-16 |
| `media3-cast-integration` | 1.9.0 | 2026-04-16 |
| `media3-inspector-metadata-thumbnails` | 1.9.0 | 2026-04-16 |

## Maintenance policy

- When a new Media3 release ships, file a Feature Issue titled `Audit pack for Media3 x.y.z` in the Issues database.
- Append a new section to this file summarizing the release.
- Re-review every skill whose `metadata.last_reviewed` is older than 90 days against the new release.
- Bump `metadata.target_media3_version` and `metadata.last_reviewed` only after the Phase 6 peer review sign-off.
