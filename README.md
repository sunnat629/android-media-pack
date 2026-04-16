# shunnek-media-pack

A curated pack of Android Skills for **AndroidX Media3 1.9.0**, authored by **Shunnek Labs** in collaboration with Kotlin User Group Bangladesh.

Each skill targets a production workflow an Android engineer hits when building media apps: migration, background playback, Widevine DRM, Compose UI, adaptive streaming, Cast, and off-player inspection.

## Skills

- [`migrate-exoplayer-to-media3`](.skills/media/migrate-exoplayer-to-media3/SKILL.md) (v1.1)
- [`media3-background-playback-service`](.skills/media/media3-background-playback-service/SKILL.md) (v1.0)
- [`media3-drm-widevine-setup`](.skills/media/media3-drm-widevine-setup/SKILL.md) (v1.0)
- [`media3-compose-ui-material3`](.skills/media/media3-compose-ui-material3/SKILL.md) (v1.0)
- [`media3-hls-dash-adaptive-streaming`](.skills/media/media3-hls-dash-adaptive-streaming/SKILL.md) (v1.0)
- [`media3-cast-integration`](.skills/media/media3-cast-integration/SKILL.md) (v1.0)
- [`media3-inspector-metadata-thumbnails`](.skills/media/media3-inspector-metadata-thumbnails/SKILL.md) (v1.0)

All skills pin `metadata.target_media3_version` to **1.9.0** and are reviewed against the canonical [Media3 release notes](https://developer.android.com/jetpack/androidx/releases/media3).

## Use

1. Drop `.skills/media/<skill-name>/SKILL.md` into your project's `.skills/` directory.
2. Verify with your agent runner:
    - **Android CLI:** `android skills list`
    - **Android Studio:** `@<skill-name>` in the Gemini chat
    - **Claude Code / Cursor:** invoke with a natural-language prompt matching the `description` field

See [COMPATIBILITY.md](COMPATIBILITY.md) for the supported toolchain and platform matrix.

## Docs

- [RELEASES.md](RELEASES.md) — Media3 release history (1.0.0 to 1.9.0) and the pack's version mapping
- [REFERENCES.md](REFERENCES.md) — trusted canonical sources and reference repositories
- [CHANGELOG.md](CHANGELOG.md) — pack release notes
- [COMPATIBILITY.md](COMPATIBILITY.md) — supported Media3, minSdk, AGP, Kotlin versions
- [CONTRIBUTING.md](CONTRIBUTING.md) — how to contribute a skill, a fix, or a doc improvement
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) — Contributor Covenant 2.1

## Contributing

All contributions are welcome. Start by reading [CONTRIBUTING.md](CONTRIBUTING.md) and opening an Issue using one of the templates under [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE/).

## License

Licensed under the [Apache License, Version 2.0](LICENSE). Copyright 2026 Shunnek Labs and contributors.
