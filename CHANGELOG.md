# Changelog

All notable changes to this pack are documented here. Format follows [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/) and the pack adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- **README install paths.** Replaced the unsupported `.skills/` target with `.agents/skills/` as the cross-agent default, and added a per-agent discovery matrix covering Claude Code, GitHub Copilot (CLI, VS Code, cloud), OpenCode, Gemini CLI, OpenAI Codex, and Cursor. A bare `.skills/` at the repo root is not scanned by any supported agent. (#77)

## [1.2.1] - 2026-04-17

### Changed
- **Renamed repository** from `shunnek-media-pack` to `android-media-skill`. Pack author remains **Shunnek Labs**. Individual skill names are unchanged.
- README rewritten: flat install with a single `tar --strip-components=2` one-liner landing files at `.skills/media/<name>/SKILL.md`, domain-grouped skill table, `Install` and `Use-with-agent` sections, badges.
- Removed `RELEASES.md`. Media3 version matrix lives in `REFERENCES.md`.
- Consolidated issue templates: `bug_report` and `feature_request` only. Questions use GitHub Discussions via a contact link in `config.yml`.
- Slimmed `COMPATIBILITY.md` to a single toolchain table + device matrix.
- Trimmed `CONTRIBUTING.md`: removed agent-ops phase workflow and DCO paragraph.

### Added
- `.github/workflows/release.yml` auto-publishes a GitHub Release on every `v*.*.*` tag push, using the matching `CHANGELOG.md` section as the release body.
- `.github/workflows/bootstrap-releases.yml` (one-shot) seeded the historical `v1.0.0`, `v1.1.0`, `v1.2.0` Releases.
- `SECURITY.md` published.
- README status badges: License, CI, Media3 pin.

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

[Unreleased]: https://github.com/sunnat629/android-media-skill/compare/v1.2.1...HEAD
[1.2.1]: https://github.com/sunnat629/android-media-skill/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/sunnat629/android-media-skill/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/sunnat629/android-media-skill/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/sunnat629/android-media-skill/releases/tag/v1.0.0
