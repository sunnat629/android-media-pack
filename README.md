# shunnek-media-pack

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![CI](https://github.com/sunnat629/shunnek-media-pack/actions/workflows/ci.yml/badge.svg)](https://github.com/sunnat629/shunnek-media-pack/actions/workflows/ci.yml)
[![Media3](https://img.shields.io/badge/Media3-1.9.0-brightgreen.svg)](https://developer.android.com/jetpack/androidx/releases/media3)
[![minSdk](https://img.shields.io/badge/minSdk-21-orange.svg)](COMPATIBILITY.md)

A curated pack of Android Skills for **AndroidX Media3 1.9.0**, authored by **Shunnek Labs** in collaboration with Kotlin User Group Bangladesh.

Each skill is a single `SKILL.md` file that teaches an AI coding agent (Claude Code, Cursor, Cline, Android Studio Gemini, and compatible runners) how to perform a specific Media3 task in your Android project. Skills are not runtime code, they are instructions the agent reads when you ask it to do something.

## Skills

### Tier 1 (v1.0)

- [`migrate-exoplayer-to-media3`](.skills/media/migrate-exoplayer-to-media3/SKILL.md) (v1.1)
- [`media3-background-playback-service`](.skills/media/media3-background-playback-service/SKILL.md) (v1.0)
- [`media3-drm-widevine-setup`](.skills/media/media3-drm-widevine-setup/SKILL.md) (v1.0)
- [`media3-compose-ui-material3`](.skills/media/media3-compose-ui-material3/SKILL.md) (v1.0)

### Tier 2 (v1.1)

- [`media3-hls-dash-adaptive-streaming`](.skills/media/media3-hls-dash-adaptive-streaming/SKILL.md) (v1.0)
- [`media3-cast-integration`](.skills/media/media3-cast-integration/SKILL.md) (v1.0)
- [`media3-inspector-metadata-thumbnails`](.skills/media/media3-inspector-metadata-thumbnails/SKILL.md) (v1.0)

### Tier 3 (v1.2)

- [`media3-live-streaming`](.skills/media/media3-live-streaming/SKILL.md) (v1.0)
- [`media3-live-only-streaming`](.skills/media/media3-live-only-streaming/SKILL.md) (v1.0)
- [`media3-audio-playback`](.skills/media/media3-audio-playback/SKILL.md) (v1.0)
- [`media3-video-playback`](.skills/media/media3-video-playback/SKILL.md) (v1.0)
- [`media3-vod-playback`](.skills/media/media3-vod-playback/SKILL.md) (v1.0)
- [`media3-datasources-networking`](.skills/media/media3-datasources-networking/SKILL.md) (v1.0)
- [`media3-lifecycle-state`](.skills/media/media3-lifecycle-state/SKILL.md) (v1.0)
- [`media3-ads-ima`](.skills/media/media3-ads-ima/SKILL.md) (v1.0)
- [`media3-analytics-telemetry`](.skills/media/media3-analytics-telemetry/SKILL.md) (v1.0)
- [`media3-bandwidth-abr`](.skills/media/media3-bandwidth-abr/SKILL.md) (v1.0)
- [`migrate-xml-ui-to-compose`](.skills/media/migrate-xml-ui-to-compose/SKILL.md) (v1.0)

All skills pin `metadata.target_media3_version` to **1.9.0** and are reviewed against the canonical [Media3 release notes](https://developer.android.com/jetpack/androidx/releases/media3).

## Install

Skills live in a `.skills/` directory at the root of your Android project. Most agent runners auto-discover them. Pick one of the options below.

### Option A: Git submodule (recommended, stays up to date)

Use this if you want to pull in new skill versions with a simple `git submodule update`.

```bash
cd your-android-project

# Add the pack as a submodule
git submodule add https://github.com/sunnat629/shunnek-media-pack .skills/shunnek-media-pack

# Commit the submodule pointer
git commit -m "chore: add shunnek-media-pack skills"
```

Your agent will see the skills at `.skills/shunnek-media-pack/.skills/media/<skill-name>/SKILL.md`.

To update later:

```bash
git submodule update --remote .skills/shunnek-media-pack
git commit -am "chore: bump shunnek-media-pack"
```

### Option B: Sparse clone (only the `.skills/media` directory)

Use this if you want to vendor the skills without the rest of the repo (CI, docs, scripts).

```bash
cd your-android-project

git clone --depth 1 --filter=blob:none --sparse \
    https://github.com/sunnat629/shunnek-media-pack \
    .skills/shunnek-media-pack

cd .skills/shunnek-media-pack
git sparse-checkout set .skills/media
```

### Option C: Copy a single skill

Use this when you only need one or two skills and do not want a dependency.

```bash
mkdir -p your-android-project/.skills/media/media3-background-playback-service
curl -L -o your-android-project/.skills/media/media3-background-playback-service/SKILL.md \
    https://raw.githubusercontent.com/sunnat629/shunnek-media-pack/main/.skills/media/media3-background-playback-service/SKILL.md
```

Repeat for each skill you need. Commit the `SKILL.md` files into your own repo.

### Option D: Pin to a release tag

For reproducibility, pin to a specific pack version.

```bash
git submodule add -b v1.2.0 https://github.com/sunnat629/shunnek-media-pack .skills/shunnek-media-pack
```

or for sparse clone:

```bash
git clone --depth 1 --branch v1.2.0 --filter=blob:none --sparse \
    https://github.com/sunnat629/shunnek-media-pack \
    .skills/shunnek-media-pack
```

### Resulting layout

After any option, your project root should look like this:

```text
your-android-project/
├── .skills/
│   └── shunnek-media-pack/
│       └── .skills/
│           └── media/
│               ├── migrate-exoplayer-to-media3/SKILL.md
│               ├── media3-background-playback-service/SKILL.md
│               └── ...
├── app/
├── build.gradle.kts
└── settings.gradle.kts
```

Some agent runners expect skills at `.skills/<name>/SKILL.md` directly. If that is your runner, use **Option C** to flatten the layout.

## Use with your AI agent

Skills are triggered by their `description` field. You do not import or reference them explicitly, you write a natural-language prompt that matches one.

### Claude Code

```bash
# In your project root, start Claude Code
claude
```

Then prompt naturally:

> Migrate this project from ExoPlayer 2.x to Media3 1.9.0.

Claude will auto-discover `migrate-exoplayer-to-media3/SKILL.md` from `.skills/` and follow its anatomy.

### Cursor / Cline / Continue

Paste the skill's description into the chat, or reference the file directly:

> Using `.skills/shunnek-media-pack/.skills/media/media3-drm-widevine-setup/SKILL.md`, set up Widevine for our DASH stream at `https://example.com/stream.mpd`.

### Android Studio (Gemini, when skills GA)

```text
@migrate-exoplayer-to-media3
```

### `android` CLI (when skills GA)

```bash
android skills list
android skills run media3-background-playback-service
```

### What the agent does

For every skill, the agent will:

1. **Plan** — grep the codebase, list affected files, flag risky call sites.
2. **Pin** Media3 1.9.0 dependencies in `libs.versions.toml` and the module `build.gradle.kts`.
3. **Edit code** following the skill's RIGHT / WRONG patterns.
4. **Flag pitfalls** (HDCP gating, foreground service rules, ProGuard, and so on) for your review.

Review the agent's diff, run your tests, and commit.

## Skill-to-task map

| You want to | Skill |
|---|---|
| Move off ExoPlayer 2.x | `migrate-exoplayer-to-media3` |
| Background audio + lock-screen controls | `media3-background-playback-service` |
| Widevine DRM, online + offline licenses | `media3-drm-widevine-setup` |
| Compose-native player UI | `media3-compose-ui-material3` |
| HLS or DASH adaptive streaming | `media3-hls-dash-adaptive-streaming` |
| Cast to Chromecast | `media3-cast-integration` |
| Read metadata or extract thumbnails off-player | `media3-inspector-metadata-thumbnails` |
| Live stream with DVR window | `media3-live-streaming` |
| Live with no DVR (sports, auctions) | `media3-live-only-streaming` |
| Audio-only (music, podcasts, audiobooks) | `media3-audio-playback` |
| Video playback (HDR, PiP, cutouts) | `media3-video-playback` |
| VOD with resume + autoadvance | `media3-vod-playback` |
| Pick OkHttp / Cronet / HttpEngine + cache | `media3-datasources-networking` |
| Lifecycle, process death, MediaController wiring | `media3-lifecycle-state` |
| Google IMA ads (CSAI or DAI) | `media3-ads-ima` |
| Mux, Bitmovin, FastPix, or in-house analytics | `media3-analytics-telemetry` |
| Tune ABR and bandwidth estimation | `media3-bandwidth-abr` |
| Move XML `PlayerView` to Compose | `migrate-xml-ui-to-compose` |

## Relationship to android/skills

`shunnek-media-pack` complements [android/skills](https://github.com/android/skills) with Media3-specific depth. Both use the same `SKILL.md` format and can live in the same `.skills/` directory. If a skill exists in both packs, prefer `android/skills` as the canonical source.

## Docs

- [REFERENCES.md](REFERENCES.md) — trusted canonical sources, reference repositories, and pack-to-Media3 version matrix
- [COMPATIBILITY.md](COMPATIBILITY.md) — supported Media3, minSdk, AGP, Kotlin versions
- [CHANGELOG.md](CHANGELOG.md) — pack release notes
- [CONTRIBUTING.md](CONTRIBUTING.md) — how to contribute a skill, a fix, or a doc improvement
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) — Contributor Covenant 2.1
- [SECURITY.md](SECURITY.md) — vulnerability reporting policy

## Contributing

All contributions are welcome. Start by reading [CONTRIBUTING.md](CONTRIBUTING.md) and opening an Issue using one of the templates under [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE/). General questions go to [Discussions](https://github.com/sunnat629/shunnek-media-pack/discussions).

## License

Licensed under the [Apache License, Version 2.0](LICENSE). Copyright 2026 Shunnek Labs and contributors.
