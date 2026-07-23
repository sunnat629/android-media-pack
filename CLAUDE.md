# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

**android-media-pack** — a pack of 31 AndroidX Media3 skills (Markdown `SKILL.md` files) for AI coding agents, plus an installer CLI and a Compose demo app. The product is the Markdown itself: skills are documentation artifacts pinned to a specific Media3 version (see `MEDIA3_VERSION`, currently 1.10.1), not compiled code.

- `.skills/media/<area>/<skill-name>/SKILL.md` — source of truth for all skills, organized by area (`playback/`, `streaming/`, `ui/`, `migration/`, `delivery/`, `protection/`, `ads-analytics/`, `background/`, `processing/`, `inspector/`, `testing/`, plus the top-level `streaming-media-architecture`).
- `bin/android-media-skill` — bash CLI (`install` / `update` / `list` / `doctor`) that downloads the repo tarball and flattens `.skills/media/**` into a target project's `.agents/skills/<skill-name>/` (override destination with `MEDIA_SKILLS_DEST`).
- `scripts/install-media-skills.sh` — one-line curl installer with the same flattening logic.
- `sampleApp/` — standalone Gradle project (single `:app` module, one `MainActivity.kt`) demonstrating Media3 Compose playback with HLS/DASH streams.
- `llms.txt` and `README.md` — discovery surfaces; the skill tables in both must stay in sync with `.skills/media/`.
- `.agent/skills/` — third-party skills installed *into* this repo for the local agent; not part of the shipped pack. Do not edit or ship these.

## Commands

```bash
# Validate skill body sizes (CI-enforced hard ceiling 20,000 chars; soft 700–6,000)
./scripts/check-skill-size.sh

# Markdown lint (config in .markdownlint-cli2.jsonc)
npx markdownlint-cli2 "**/*.md"

# Shell script lint
shellcheck bin/android-media-skill scripts/*.sh run-sample-app.sh

# Build, install, and launch the sample app on a connected device/emulator
./run-sample-app.sh --serial <device-serial>
./run-sample-app.sh --clean --no-launch   # build+install only

# Sample app Gradle directly
cd sampleApp && ./gradlew assembleDebug
```

There is no test suite; CI on tags (`release.yml`) only extracts CHANGELOG notes and publishes a GitHub Release. Quality gates are the size check, markdownlint, shellcheck, and human review.

## Skill authoring rules (from CONTRIBUTING.md — enforced in review)

- Every `SKILL.md` needs YAML frontmatter: `name`, `description`, `license: Apache-2.0`, `metadata.author`, `metadata.version`, `metadata.target_media3_version`, `metadata.last_reviewed`, `metadata.keywords`.
- Body sections: **Trigger** (when an agent loads it), **Rules**, then optional **Example**, **Gradle**, **Related**, **Do Not**, **Sources**.
- Size budget: target 700–6,000 characters in the body; hard ceiling 20,000. Overflow goes to `references/*.md` companions linked with relative paths.
- Style: **no em dashes**, **no contractions** ("do not", never "don't"), bold **MUST** / **MUST NOT** / **DO NOT** / **PREFERRED**, every code block language-tagged (`kotlin`, `xml`, `toml`, `bash`).
- Citations must resolve to Media3 release notes, `androidx/media` source, or developer.android.com (see REFERENCES.md). Community posts are never primary sources.
- No `@ExperimentalApi` Media3 classes (e.g. `CompositionPlayer`) in RIGHT examples.
- Kotlin examples must compile against the pinned Media3 version.
- When adding/renaming a skill, update the tables in `README.md` and `llms.txt`.
- Branch naming: `skill/<name>`, `fix/<short>`, or `docs/<short>`. PRs are squash-merged.

## Multi-agent team workflow

When the user asks for planning or delegation, operate as a small leadership team before touching code:

**Roles**

- **PM** — clarifies scope, splits the request into small, independently mergeable tasks, and defines acceptance criteria per task.
- **CTO / System Architect** — decides technical approach, owns quality: reviews every diff before merge (or immediately after a lead agent reports done), checks skill-authoring rules above, and blocks merges that fail them.
- **UI/UX** — owns anything touching `sampleApp/` UI, README presentation, or skill readability.

**Flow**

1. PM + CTO produce a short plan: task list, owner per task, branch name per task.
2. PM/CTO assign each task to one or more lead agents (use the Agent tool; parallelize independent tasks). Lead agents may further delegate.
3. Git discipline per task:
   - Create a feature branch off `main` (or off the parent feature branch for subtasks) using the naming scheme above.
   - Commit incrementally as work progresses — small, coherent commits, not one giant commit at the end.
   - When a **subtask** finishes: squash-merge it into its parent branch.
   - When a **top-level task** finishes: push the branch and open a PR against `main` (PRs are squash-merged; follow `.github/PULL_REQUEST_TEMPLATE.md`).
4. CTO/System Architect reviews each branch before merge: run `./scripts/check-skill-size.sh`, markdownlint, and shellcheck as applicable, verify authoring rules, then approve or send back with concrete fixes.
5. PM confirms acceptance criteria are met and reports status to the user.

Never merge unreviewed work to `main`. If a task is trivial (typo-level), CTO review can happen post-hoc, but it still happens.
