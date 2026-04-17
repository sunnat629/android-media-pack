# android-media-pack

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![CI](https://github.com/sunnat629/android-media-pack/actions/workflows/ci.yml/badge.svg)](https://github.com/sunnat629/android-media-pack/actions/workflows/ci.yml)
[![Media3](https://img.shields.io/badge/Media3-1.9.0-brightgreen.svg)](https://developer.android.com/jetpack/androidx/releases/media3)

Android skills for AI coding agents to build with **AndroidX Media3 1.9.0**. 18 skills covering migration, playback, DRM, streaming, ads, and analytics. Published by **Shunnek Labs**.

Each skill is a folder with a single `SKILL.md`. There is no universal skills folder, every agent reads its own location. Pick the section for your agent below.

## Install

### 1. Android CLI / Gemini in Android Studio

User-scoped. Install once, use from any project. `android skills list` reads `~/.android/skills/`.

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
# ... 18 total

android "Migrate this project from ExoPlayer 2.x to Media3 1.9.0"
```

Gemini in Android Studio reads the same directory. Invoke with `@migrate-exoplayer-to-media3` in the chat.

### 2. Claude Code

Project-scoped: reads `.claude/skills/` in the current repo.

```bash
cd your-android-project
mkdir -p .claude/skills
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/heads/main.tar.gz \
  | tar -xz --strip-components=3 -C .claude/skills android-media-pack-main/.skills/media
```

User-scoped alternative: replace `.claude/skills` with `~/.claude/skills` to make the pack available in every project.

### 3. Cline

Project-scoped: reads `.clinerules/` as plain markdown.

```bash
cd your-android-project
mkdir -p .clinerules
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/heads/main.tar.gz \
  | tar -xz --strip-components=3 -C .clinerules android-media-pack-main/.skills/media
```

Each `SKILL.md` lands at `.clinerules/<skill-name>/SKILL.md` and Cline reads them as rules.

### 4. Junie (JetBrains AI Assistant)

Junie reads a single file, `.junie/guidelines.md`. It does not auto-discover a skills folder. Reference the pack from that guidelines file:

```bash
cd your-android-project
mkdir -p .junie
cat >> .junie/guidelines.md <<'EOF'
## Media playback
When asked to migrate or change Media3 code, follow the matching skill from
github.com/sunnat629/android-media-pack (branch main, folder .skills/media/<skill-name>/SKILL.md).

Key skills:
- `migrate-exoplayer-to-media3` for ExoPlayer 2.x migrations
- `media3-background-playback-service` for background audio
- `media3-drm-widevine-setup` for Widevine
- `media3-compose-ui-material3` for Compose player UI

Pin Media3 to 1.9.0.
EOF
```

For offline use, clone the repo somewhere on disk and inline the relevant `SKILL.md` content into `.junie/guidelines.md`.

### 5. Cursor

Cursor reads `.cursor/rules/*.mdc`, which is a **different format** (frontmatter plus `@`-scoped rules). The pack does not ship `.mdc` files yet. Workarounds:

1. Symlink or copy a specific `SKILL.md` into `.cursor/rules/`, renaming it to `<skill>.mdc`. Cursor will still parse the markdown body but frontmatter keys will be ignored.
2. Or keep the pack under `.claude/skills/` (see above) and reference the file in a Cursor composer prompt.

First-class `.mdc` generation is tracked as a future Issue.

### Reproducible installs

Pin a release tag in place of `refs/heads/main` and update the archive prefix:

```bash
curl -sL https://github.com/sunnat629/android-media-pack/archive/refs/tags/v1.2.0.tar.gz \
  | tar -xz --strip-components=3 -C ~/.android/skills android-media-pack-1.2.0/.skills/media
```

Re-run any install command to update.

## Use

Prompt the agent in plain English. It matches the right skill by reading each `description:` line in the frontmatter.

> Migrate this project from ExoPlayer 2.x to Media3 1.9.0.

## Test it works

Dry-run prompt against any agent, then check the diff. Pass criteria:

- Gradle: `com.google.android.exoplayer:exoplayer-*` removed, `androidx.media3:media3-*:1.9.0` added.
- Zero `com.google.android.exoplayer2` imports under `app/src`.
- `SimpleExoPlayer` replaced by `ExoPlayer`.
- `MediaSessionConnector` replaced by `MediaSession` inside a `MediaSessionService`.
- DRM wired through `DefaultDrmSessionManagerProvider`.
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

Complements [android/skills](https://github.com/android/skills). Same folder layout, same frontmatter keys. Prefer the canonical `android/skills` when a skill exists in both.

## Docs

[Changelog](CHANGELOG.md) · [Compatibility](COMPATIBILITY.md) · [Contributing](CONTRIBUTING.md) · [References](REFERENCES.md) · [Security](SECURITY.md) · [Code of Conduct](CODE_OF_CONDUCT.md)

Questions go to [Discussions](https://github.com/sunnat629/android-media-pack/discussions). Bugs go to [Issues](https://github.com/sunnat629/android-media-pack/issues/new/choose).

## License

[Apache License 2.0](LICENSE). Copyright 2026 Shunnek Labs and contributors.
