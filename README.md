# android-media-pack

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![CI](https://github.com/sunnat629/android-media-pack/actions/workflows/ci.yml/badge.svg)](https://github.com/sunnat629/android-media-pack/actions/workflows/ci.yml)
[![Media3](https://img.shields.io/badge/Media3-1.9.0-brightgreen.svg)](https://developer.android.com/jetpack/androidx/releases/media3)

Android skills for AI coding agents to build with **AndroidX Media3 1.9.0**. Drop them into your project or your global skills directory, then prompt your agent. 18 skills covering migration, playback, DRM, streaming, ads, and analytics. Published by **Shunnek Labs**.

## Install

Pick the path that matches your agent. The pack ships 18 skill folders, each containing a single `SKILL.md`.

### 1. Android CLI (recommended for terminal use)

Install once, use from any Android project.

```bash
mkdir -p ~/.android/skills
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/heads/main.tar.gz \
  | tar -xz --strip-components=3 -C ~/.android/skills android-media-pack-main/.skills/media
```

Verify:

```bash
android skills list
# migrate-exoplayer-to-media3
# media3-background-playback-service
# media3-drm-widevine-setup
# ... 18 total
```

Then from any project:

```bash
android "Migrate this project from ExoPlayer 2.x to Media3 1.9.0"
```

**Prefer a grouped install?** If you want all 18 skills namespaced under a single folder (for easier removal), install into a sub-directory:

```bash
mkdir -p ~/.android/skills/android-media-pack
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/heads/main.tar.gz \
  | tar -xz --strip-components=3 -C ~/.android/skills/android-media-pack android-media-pack-main/.skills/media
```

Note: whether the Android CLI discovers skills nested under a sub-directory depends on your CLI version. If `android skills list` returns empty after a nested install, fall back to the flat install above.

### 2. Project-level (Claude Code, Cursor, Cline, Continue)

Scoped to one repo. Commit the pack so teammates get the same skills.

```bash
cd your-android-project
mkdir -p .skills/android-media-pack
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/heads/main.tar.gz \
  | tar -xz --strip-components=3 -C .skills/android-media-pack android-media-pack-main/.skills/media
```

Result:

```text
your-android-project/
├── .skills/
│   └── android-media-pack/
│       ├── migrate-exoplayer-to-media3/SKILL.md
│       ├── media3-background-playback-service/SKILL.md
│       └── ...
├── app/
└── build.gradle.kts
```

### 3. Junie (JetBrains AI Assistant)

Junie reads project guidelines from `.junie/guidelines.md`. After a project-level install, create or append:

```bash
mkdir -p .junie
cat >> .junie/guidelines.md <<'EOF'
## Media playback
When asked to migrate or change media playback code, follow the matching skill under
`.skills/android-media-pack/<skill-name>/SKILL.md`.
Prefer `migrate-exoplayer-to-media3` for ExoPlayer 2.x migrations.
Pin Media3 to 1.9.0.
EOF
```

### 4. Gemini in Android Studio

Skills under `~/.android/skills/` (from the CLI install) are picked up automatically. Invoke in chat with `@migrate-exoplayer-to-media3`.

### Reproducible installs

For reproducible installs, pin a release tag in place of `refs/heads/main` and the matching archive prefix:

```bash
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/tags/v1.2.0.tar.gz \
  | tar -xz --strip-components=3 -C ~/.android/skills android-media-pack-1.2.0/.skills/media
```

Re-run any install command to update.

## Use

Prompt your agent in plain English. It picks the matching skill by reading each `description` line in the frontmatter.

> Migrate this project from ExoPlayer 2.x to Media3 1.9.0.

Works with Claude Code, Cursor, Cline, Continue, the Android CLI, Gemini in Android Studio, and Junie (via guidelines).

## Test it works

Use this dry-run prompt against any agent and check the resulting diff:

> Migrate this module from ExoPlayer 2.19.1 to Media3 1.9.0. Follow the project skill.

Pass criteria:

- Gradle: `com.google.android.exoplayer:exoplayer-*` removed, `androidx.media3:media3-*:1.9.0` added.
- Imports: zero `com.google.android.exoplayer2` occurrences under `app/src`.
- `SimpleExoPlayer` replaced by `ExoPlayer`.
- `MediaSessionConnector` replaced by `MediaSession` inside a `MediaSessionService`.
- DRM wired through `DefaultDrmSessionManagerProvider`, not on a `MediaSource.Factory`.
- `./gradlew :app:assembleDebug` succeeds.

Any failure is a bug against the matching skill. File it via [Issues](https://github.com/sunnat629/android-media-pack/issues/new/choose).

## Skills

| Domain | Skill |
|---|---|
| **Migration** | [`migrate-exoplayer-to-media3`](.skills/media/migrate-exoplayer-to-media3/SKILL.md) · [`migrate-xml-ui-to-compose`](.skills/media/migrate-xml-ui-to-compose/SKILL.md) |
| **Core** | [`media3-background-playback-service`](.skills/media/media3-background-playback-service/SKILL.md) · [`media3-lifecycle-state`](.skills/media/media3-lifecycle-state/SKILL.md) |
| **UI** | [`media3-compose-ui-material3`](.skills/media/media3-compose-ui-material3/SKILL.md) · [`media3-video-playback`](.skills/media/media3-video-playback/SKILL.md) |
| **Streaming** | [`media3-hls-dash-adaptive-streaming`](.skills/media/media3-hls-dash-adaptive-streaming/SKILL.md) · [`media3-live-streaming`](.skills/media/media3-live-streaming/SKILL.md) · [`media3-live-only-streaming`](.skills/media/media3-live-only-streaming/SKILL.md) · [`media3-vod-playback`](.skills/media/media3-vod-playback/SKILL.md) · [`media3-audio-playback`](.skills/media/media3-audio-playback/SKILL.md) |
| **Delivery** | [`media3-datasources-networking`](.skills/media/media3-datasources-networking/SKILL.md) · [`media3-bandwidth-abr`](.skills/media/media3-bandwidth-abr/SKILL.md) · [`media3-cast-integration`](.skills/media/media3-cast-integration/SKILL.md) |
| **Protection** | [`media3-drm-widevine-setup`](.skills/media/media3-drm-widevine-setup/SKILL.md) |
| **Ads & analytics** | [`media3-ads-ima`](.skills/media/media3-ads-ima/SKILL.md) · [`media3-analytics-telemetry`](.skills/media/media3-analytics-telemetry/SKILL.md) |
| **Off-player** | [`media3-inspector-metadata-thumbnails`](.skills/media/media3-inspector-metadata-thumbnails/SKILL.md) |

All skills pin to Media3 **1.9.0**. See [COMPATIBILITY.md](COMPATIBILITY.md) for the toolchain matrix.

## Related

Complements [android/skills](https://github.com/android/skills). Same format, same folder layout. Prefer the canonical `android/skills` when a skill exists in both.

## Docs

[Changelog](CHANGELOG.md) · [Compatibility](COMPATIBILITY.md) · [Contributing](CONTRIBUTING.md) · [References](REFERENCES.md) · [Security](SECURITY.md) · [Code of Conduct](CODE_OF_CONDUCT.md)

Questions go to [Discussions](https://github.com/sunnat629/android-media-pack/discussions). Bugs go to [Issues](https://github.com/sunnat629/android-media-pack/issues/new/choose).

## License

[Apache License 2.0](LICENSE). Copyright 2026 Shunnek Labs and contributors.
