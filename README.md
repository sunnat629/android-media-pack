# android-media-skill

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![CI](https://github.com/sunnat629/android-media-skill/actions/workflows/ci.yml/badge.svg)](https://github.com/sunnat629/android-media-skill/actions/workflows/ci.yml)
[![Media3](https://img.shields.io/badge/Media3-1.10.1-brightgreen.svg)](https://developer.android.com/jetpack/androidx/releases/media3)

Android skills for AI coding agents to build with **AndroidX Media3 1.10.1**. Drop them into your project and prompt your agent. 19 skills covering architecture, migration, playback, DRM, streaming, ads, and analytics. Published by **Shunnek Labs**.

## Install

With the CLI:

```bash
curl -fsSL https://raw.githubusercontent.com/sunnat629/android-media-skill/main/bin/android-media-skill \
  -o android-media-skill
chmod +x android-media-skill
./android-media-skill install /path/to/your-android-project
```

CLI commands:

```bash
./android-media-skill install /path/to/project
./android-media-skill update /path/to/project
./android-media-skill list /path/to/project
./android-media-skill doctor /path/to/project
```

With a repo link:

```bash
cd your-android-project
curl -fsSL https://raw.githubusercontent.com/sunnat629/android-media-skill/main/scripts/install-media-skills.sh \
  | bash -s -- . https://github.com/sunnat629/android-media-skill main
```

From a tarball:

```bash
cd your-android-project
rm -rf .skills/media
mkdir -p .skills/media
curl -sL https://github.com/sunnat629/android-media-skill/archive/refs/heads/main.tar.gz \
  | tar -xz --strip-components=3 -C .skills/media android-media-skill-main/.skills/media
```

All install commands replace only `.skills/media`.

Result:

```text
your-android-project/
├── .skills/media/
│   ├── migrate-exoplayer-to-media3/SKILL.md
│   ├── media3-background-playback-service/SKILL.md
│   └── ...
├── app/
└── build.gradle.kts
```

Re-run the command to update. For reproducible installs, swap `refs/heads/main` for a tagged release such as `refs/tags/v1.2.0` and `android-media-skill-main` for `android-media-skill-1.2.0`.

## Use

Prompt your agent in natural language. It picks the matching skill by reading each `description` line.

> Migrate this project from ExoPlayer 2.x to Media3 1.10.1.

Works with Claude Code, Cursor, Cline, Continue, and any runner that reads `.skills/`.

## Skills

| Domain | Skill |
|---|---|
| **Architecture** | [`streaming-media-architecture`](.skills/media/streaming-media-architecture/SKILL.md) |
| **Migration** | [`migrate-exoplayer-to-media3`](.skills/media/migrate-exoplayer-to-media3/SKILL.md) · [`migrate-xml-ui-to-compose`](.skills/media/migrate-xml-ui-to-compose/SKILL.md) |
| **Core** | [`media3-background-playback-service`](.skills/media/media3-background-playback-service/SKILL.md) · [`media3-lifecycle-state`](.skills/media/media3-lifecycle-state/SKILL.md) |
| **UI** | [`media3-compose-ui-material3`](.skills/media/media3-compose-ui-material3/SKILL.md) · [`media3-video-playback`](.skills/media/media3-video-playback/SKILL.md) |
| **Streaming** | [`media3-hls-dash-adaptive-streaming`](.skills/media/media3-hls-dash-adaptive-streaming/SKILL.md) · [`media3-live-streaming`](.skills/media/media3-live-streaming/SKILL.md) · [`media3-live-only-streaming`](.skills/media/media3-live-only-streaming/SKILL.md) · [`media3-vod-playback`](.skills/media/media3-vod-playback/SKILL.md) · [`media3-audio-playback`](.skills/media/media3-audio-playback/SKILL.md) |
| **Delivery** | [`media3-datasources-networking`](.skills/media/media3-datasources-networking/SKILL.md) · [`media3-bandwidth-abr`](.skills/media/media3-bandwidth-abr/SKILL.md) · [`media3-cast-integration`](.skills/media/media3-cast-integration/SKILL.md) |
| **Protection** | [`media3-drm-widevine-setup`](.skills/media/media3-drm-widevine-setup/SKILL.md) |
| **Ads & analytics** | [`media3-ads-ima`](.skills/media/media3-ads-ima/SKILL.md) · [`media3-analytics-telemetry`](.skills/media/media3-analytics-telemetry/SKILL.md) |
| **Off-player** | [`media3-inspector-metadata-thumbnails`](.skills/media/media3-inspector-metadata-thumbnails/SKILL.md) |

All skills pin to Media3 **1.10.1**. See [COMPATIBILITY.md](COMPATIBILITY.md) for the toolchain matrix.

## Related

Complements [android/skills](https://github.com/android/skills). Same format, same folder. Prefer the canonical `android/skills` when a skill exists in both.

## Docs

[Changelog](CHANGELOG.md) · [Compatibility](COMPATIBILITY.md) · [Contributing](CONTRIBUTING.md) · [References](REFERENCES.md) · [Security](SECURITY.md) · [Code of Conduct](CODE_OF_CONDUCT.md)

Questions go to [Discussions](https://github.com/sunnat629/android-media-skill/discussions). Bugs go to [Issues](https://github.com/sunnat629/android-media-skill/issues/new/choose).

## License

[Apache License 2.0](LICENSE). Copyright 2026 Shunnek Labs and contributors.
