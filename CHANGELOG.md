# Changelog

All notable changes to this pack are documented here. Format follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/) and the pack adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- **Renamed repository** from `shunnek-media-pack` to `android-media-skill`. Pack author remains **Shunnek Labs**.
- README simplified: flat install layout, domain-grouped skill table, removed Tier headings and duplicate task map.
- Removed `RELEASES.md`. Media3 version matrix lives in `REFERENCES.md`.
- Consolidated issue templates: Bug + Feature only. Questions use GitHub Discussions.
- Slimmed `COMPATIBILITY.md` and `CONTRIBUTING.md`.

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
- GitHub Actions CI: markdownlint, YAML frontmatter validation, skill size guard, shellcheck.
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

[Unreleased]: https://github.com/sunnat629/android-media-skill/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/sunnat629/android-media-skill/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/sunnat629/android-media-skill/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/sunnat629/android-media-skill/releases/tag/v1.0.0
