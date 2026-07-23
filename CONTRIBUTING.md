# Contributing to android-media-pack

Thanks for wanting to contribute. This pack ships Android Skills that engineers and AI agents trust on first read and still trust six months later. Contributions that uphold that bar are welcome.

## Before you start

1. Read [COMPATIBILITY.md](COMPATIBILITY.md) to confirm your toolchain matches the pack's target.
2. Read [REFERENCES.md](REFERENCES.md). Citations **MUST** resolve to one of the canonical sources or to the current Media3 release notes.
3. Agree to the [Code of Conduct](CODE_OF_CONDUCT.md).
4. Open an Issue before you start coding non-trivial changes. Use the templates in `.github/ISSUE_TEMPLATE/`. For general questions, use [Discussions](https://github.com/sunnat629/android-media-pack/discussions).

## Issue types

- **Bug:** a shipped skill has an incorrect API, an example that does not compile, or a claim that is false on the current Media3 release. Docs typos also use the Bug template with a `type-docs` label.
- **Feature:** a new skill, or a new major section inside an existing skill.

## Skill anatomy

Every skill `SKILL.md` **MUST** stay compact and include:

1. **YAML frontmatter** with `name`, `description`, SPDX `license: Apache-2.0`, `metadata.author`, `metadata.version`, `metadata.target_media3_version`, `metadata.last_reviewed`, and `metadata.keywords`.
2. **Trigger** describing when an agent should load the skill.
3. **Rules** with hard project constraints and current Media3 guidance.
4. **Example**, **Gradle**, **Related**, **Do Not**, or **Sources** sections when they add useful signal.

## Size budget

- Target **700 to 6,000 characters** in the `SKILL.md` body.
- Hard ceiling **20,000 characters**. CI enforces this via `scripts/check-skill-size.sh`. Move overflow to `references/*.md` companions and link with relative paths.

## Style

- **No em dashes.** Use commas, periods, or restructure.
- **No contractions.** Always write full forms ("is not", "do not", "cannot").
- Use **MUST**, **MUST NOT**, **DO NOT**, **PREFERRED** in bold.
- Every code block **MUST** be tagged with a language (`kotlin`, `xml`, `toml`, `bash`).

## Pull request flow

1. Fork the repo, branch from `main`, name your branch `skill/<name>`, `fix/<short>`, or `docs/<short>`.
2. Follow the PR template in [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md).
3. CI **MUST** be green before review (markdownlint, YAML frontmatter validation, skill size guard, shellcheck).
4. At least one approving review is required.
5. We squash-merge. The commit message will include the issue reference.

## Citation policy

When a skill cites an API, behavior, or version claim:

- Primary: Media3 release notes + `androidx/media` source.
- Secondary: developer.android.com guides.
- Tertiary: the reference repositories in [REFERENCES.md](REFERENCES.md).
- Community posts are never a primary citation in a skill's RIGHT path.

If a community source contradicts the release notes, trust the release notes and file a `type-docs` Issue against the contradicting source.

## Out of scope for v2.x

- `media3-low-latency-live` (LL-HLS, LL-DASH)
- WebRTC / WHIP integration
- Any API marked `@ExperimentalApi` in Media3 1.10.1, including `CompositionPlayer`.

`media3-transformer-editing` and `media3-workmanager-offline-ops` shipped in 2.0.0 and are no longer out of scope. New skill ideas become Feature Issues first, not surprise skills.

## License and DCO

All contributions are accepted under the [Apache License 2.0](LICENSE) as per Section 5 of the license text. No CLA and no DCO sign-off is required.

## Reviewing skills

Reviewers should verify:

- All **MUST** and **MUST NOT** statements are accurate against the current Media3 release.
- Every Kotlin code block compiles in a toy app at the pinned Media3 version.
- Examples are compact and compile against the pinned Media3 version when present.
- Body is between 700 and 6,000 characters unless there is a clear reason to exceed the soft target.
- `metadata.last_reviewed` is the date of the publish commit.
- No `@ExperimentalApi` class appears in any RIGHT example.

## Relationship to android/skills

This pack complements [android/skills](https://github.com/android/skills) with Media3-specific depth. If the skill you are proposing is platform-wide rather than Media3-specific, consider upstreaming it to `android/skills` instead.
