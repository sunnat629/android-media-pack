# Changelog

All notable changes to this pack are documented here. Format follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/) and the pack adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Added a root `llms.txt` retrieval index and clearer README discovery copy for search engines, repository search, and LLM-powered coding agents.
- GitHub Actions CI (`ci.yml`): markdownlint, SKILL.md frontmatter validation (`scripts/check-skill-frontmatter.sh`), the skill size guard, and shellcheck. This delivers the CI that the 1.2.0 notes announced but that never shipped.
- Gradle wrapper for `sampleApp` so `./gradlew` works out of the box.

### Changed

- `bin/android-media-skill` now reports only the skills it actually installed, records installs in `.android-media-pack-manifest`, removes skills that disappear upstream on update, warns on duplicate skill folder names, and supports absolute `MEDIA_SKILLS_DEST` paths.
- `scripts/install-media-skills.sh` is a thin wrapper delegating to the bundled CLI instead of a diverging copy of the install logic.
- Renamed `VERSION` to `MEDIA3_VERSION` to make clear it pins the Media3 target, not the pack version.
- Linked previously orphaned `references/*.md` companions from the DRM, ExoPlayer migration, and HLS/DASH skills.
- Sample app: state now survives rotation, UI state derives from `Player.Listener` instead of polling, playback errors surface with retry, and Media3 unstable API opt-ins are declared.
- Internet-verified currency pass over all 31 skills against Media3 1.10.1 sources and current official guides:
  - `media3-cast-integration`: `CastPlayer.Builder`/`RemoteCastPlayer.Builder` (constructors deprecated since 1.9.0), automatic local/remote transitions, `DefaultCastOptionsProvider` meta-data, `MediaTransferReceiver`.
  - `media3-bandwidth-abr` and the HLS/DASH `abr-tuning.md` reference: no-arg `TrackSelectionParameters.Builder()` (the `Context` overload is deprecated and ignored); removed a false zero-tracks claim and an unsourced "PreloadManager memory guard" claim.
  - `media3-workmanager-offline-ops`: rebuilt around `DownloadService`, `DownloadManager`, `DownloadRequest`, `DownloadIndex`, and `Requirements`, with `WorkManagerScheduler` in its real role and the targetSdk 35 `dataSync` 6-hour cap.
  - `media3-tv-leanback-ui`: Compose for TV is now the primary path; Leanback and `media3-ui-leanback` are maintenance-only (androidx.leanback is deprecated).
  - `media3-adaptive-compose-ui`: replaced deprecated `WindowWidthSizeClass` buckets with `currentWindowAdaptiveInfo()` breakpoint checks.
  - `media3-inspector-metadata-thumbnails`: frame extraction now points at the `media3-inspector-frame` module and `androidx.media3.inspector.*` classes (exoplayer-package variants removed in 1.11).
  - `manifest-errors.md`: `BEHIND_LIVE_WINDOW` recovery now calls `prepare()` after `seekToDefaultPosition()`.
  - Migration references: corrected the `PlayerView` mapping (StyledPlayerView pre-step), fixed `rewrite-imports.sh` producing nonexistent `androidx.media3.common.ExoPlayer` imports, and corrected version attribution in the 1.10 API-changes notes.
  - Clarity fixes: concrete SSAI one-per-playlist rule (ads), HttpEngine/Cronet stack priority (datasources), `FOREGROUND_SERVICE_MEDIA_PLAYBACK` and service intent-filters (background, Auto), JitPack requirement (MIDI), `APPLICATION_SS` MIME fallback (SmoothStreaming), pre-prepare seeks (VOD), SurfaceView-first surface rule (video), Jetpack XR `SpatialExternalSurface` anchors (XR).

### Removed

- One-off workflows `bootstrap-releases.yml`, `release-v1.2.1.yml`, and `release-v1.2.2.yml`, which could re-tag and re-publish releases on any edit to their own files.

### Fixed

- `scripts/check-skill-size.sh` no longer counts a malformed frontmatter-less file as a 0-character body, so such files can no longer bypass the 20,000-character ceiling.

## [2.0.0] - 2026-05-24

### Added

- `media3-rtsp-playback` for RTSP camera feeds, LAN streams, and credential-safe failure handling.
- `media3-smoothstreaming-playback` for SmoothStreaming manifests and adaptive playback caveats.
- `media3-midi-playback` for MIDI playback dependency checks and generated-audio caveats.
- `media3-view-ui-player` for Media3 View-based `PlayerView`, XML UI, and Compose interop.
- `media3-tv-leanback-ui` for Android TV Leanback UI, D-pad focus, and overscan-safe controls.
- `media3-android-auto-media-surface` for Android Auto media sessions, browse trees, and transport controls.
- `media3-xr-media-surface` for Android XR media playback planning with current-doc verification.
- `media3-adaptive-compose-ui` for responsive Compose player UI across phones, tablets, foldables, and large screens.
- `media3-workmanager-offline-ops` for WorkManager-backed offline media operations.
- `media3-transformer-editing` for trim, transcode, export jobs, progress, and cancellation.
- `media3-video-effects-lottie-muxer` for video effects, Lottie overlays, muxing, and export boundaries.
- `media3-test-utils-robolectric` for Media3 test utilities, Robolectric patterns, and fake playback state.

### Changed

- Expanded the pack from 19 to 31 skills.
- Updated README skill map, examples, and current version line for v2.0.0.
- Updated `sampleApp` skill list and domain summary for the new protocol, device, processing, and testing skills.

## [1.5.0] - 2026-05-24

### Added

- `streaming-media-architecture` v1.1 (Media3 1.10.1) for top-level Android/KMP streaming architecture and reels/feed playback decisions.
- `bin/android-media-skill` CLI with `install`, `update`, `list`, and `doctor` commands.
- `scripts/install-media-skills.sh` for external installs from a repository link into a local agent skills folder.
- `sampleApp` with a Compose Material3 home, all 19 media skills, Media3 `ContentFrame`, and Material3 playback controls.

### Changed

- Rewrote README for readability with clearer skill descriptions, install paths, and with-vs-without-skill examples.
- Refreshed all main skills from the newer compact skill set and bumped `metadata.target_media3_version` to Media3 1.10.1.
- Updated the installer to flatten categorized source skills into agent-readable `<skill-name>/SKILL.md` folders.
- Bumped the sample app Media3 version catalog entry to 1.10.1.

## [1.2.2] - 2026-04-17

### Changed

- Frontmatter-only bump of `metadata.target_media3_version` to Media3 1.10.0. Tagged directly without a full audit pass.

## [1.2.1] - 2026-04-17

### Changed

- Renamed the repository and overhauled the README.

## [1.2.0] - 2026-04-17

### Added (Tier 3)

- `media3-live-streaming` v1.0 (Media3 1.9.0)
- `media3-datasources-networking` v1.0 (Media3 1.9.0)
- `media3-lifecycle-state` v1.0 (Media3 1.9.0)
- `media3-ads-ima` v1.0 (Media3 1.9.0)
- `media3-analytics-telemetry` v1.0 (Media3 1.9.0)
- `media3-bandwidth-abr` v1.0 (Media3 1.9.0)
- `migrate-xml-ui-to-compose` v1.0 (Media3 1.9.0)
- `media3-audio-playback` v1.0 (Media3 1.9.0)
- `media3-video-playback` v1.0 (Media3 1.9.0)
- `media3-vod-playback` v1.0 (Media3 1.9.0)
- `media3-live-only-streaming` v1.0 (Media3 1.9.0)

### Added (repo scaffolding)

- `LICENSE` (Apache 2.0), `README.md`, `CONTRIBUTING.md`, `COMPATIBILITY.md`, `CODE_OF_CONDUCT.md` (Contributor Covenant 2.1).
- PR template and issue templates, plus `config.yml`.
- GitHub Actions CI: markdownlint, YAML frontmatter validation, skill size guard, shellcheck. Correction: this workflow was announced here but never landed; the CI first shipped with the Unreleased `ci.yml` entry above.
- `scripts/check-skill-size.sh` enforcing the 20,000-character hard ceiling.

### Changed

- Dropped the trailing `## Checklist` section from every `SKILL.md` to match the canonical anatomy used by `android/skills`.
- Every `SKILL.md` frontmatter uses SPDX `license: Apache-2.0`.

## [1.1.0] - 2026-04-16

### Added (Tier 2)

- `media3-hls-dash-adaptive-streaming` v1.0 (Media3 1.9.0)
- `media3-cast-integration` v1.0 (Media3 1.9.0)
- `media3-inspector-metadata-thumbnails` v1.0 (Media3 1.9.0)
- `REFERENCES.md` with canonical docs and reference repositories.

## [1.0.0] - 2026-04-16

### Added (Tier 1)

- `migrate-exoplayer-to-media3` v1.1 (Media3 1.9.0)
- `media3-background-playback-service` v1.0 (Media3 1.9.0)
- `media3-drm-widevine-setup` v1.0 (Media3 1.9.0)
- `media3-compose-ui-material3` v1.0 (Media3 1.9.0)

### Meta

- Initial repository scaffold: SKILL anatomy, issue types, label taxonomy.

[Unreleased]: https://github.com/sunnat629/android-media-pack/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/sunnat629/android-media-pack/compare/v1.5.0...v2.0.0
[1.5.0]: https://github.com/sunnat629/android-media-pack/compare/v1.2.2...v1.5.0
[1.2.2]: https://github.com/sunnat629/android-media-pack/compare/v1.2.1...v1.2.2
[1.2.1]: https://github.com/sunnat629/android-media-pack/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/sunnat629/android-media-pack/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/sunnat629/android-media-pack/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/sunnat629/android-media-pack/releases/tag/v1.0.0
