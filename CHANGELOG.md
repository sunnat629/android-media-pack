# Changelog

All notable changes to this pack are documented here. Format follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/) and the pack adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Open-source scaffolding: `LICENSE` (Apache 2.0), `README.md`, `CONTRIBUTING.md`, `COMPATIBILITY.md`, `CODE_OF_CONDUCT.md`, PR template, issue templates, CI workflow.
- `scripts/check-skill-size.sh` enforcing the 20,000-character hard ceiling on `SKILL.md` bodies.

### Changed
- Dropped the trailing `## Checklist` section from every `SKILL.md` to match the canonical anatomy used by `android/skills`.

## [1.1.0] - 2026-04-16

### Added (Tier 2)
- `media3-hls-dash-adaptive-streaming` v1.0 (Media3 1.9.0)
- `media3-cast-integration` v1.0 (Media3 1.9.0)
- `media3-inspector-metadata-thumbnails` v1.0 (Media3 1.9.0)
- `RELEASES.md` tracking Media3 1.0.0 through 1.9.0 with the skill-to-version mapping.
- `REFERENCES.md` with canonical docs and 15+ reference repositories.

## [1.0.0] - 2026-04-16

### Added (Tier 1)
- `migrate-exoplayer-to-media3` v1.1 (Media3 1.9.0)
- `media3-background-playback-service` v1.0 (Media3 1.9.0)
- `media3-drm-widevine-setup` v1.0 (Media3 1.9.0)
- `media3-compose-ui-material3` v1.0 (Media3 1.9.0)

### Meta
- Initial repository scaffold: SKILL anatomy, issue types, label taxonomy.
- Notion-based planning hub with Tasks and Issues databases mirrored to GitHub sub-issues.

[Unreleased]: https://github.com/sunnat629/shunnek-media-pack/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/sunnat629/shunnek-media-pack/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/sunnat629/shunnek-media-pack/releases/tag/v1.0.0
