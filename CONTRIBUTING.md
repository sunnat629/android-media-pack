# Contributing to shunnek-media-pack

Thanks for wanting to contribute. This pack ships Android Skills that engineers and AI agents trust on first read and still trust six months later. Contributions that uphold that bar are welcome.

## Before you start

1. Read [COMPATIBILITY.md](COMPATIBILITY.md) to confirm your toolchain matches the pack's target.
2. Read [REFERENCES.md](REFERENCES.md). Citations **MUST** resolve to one of the canonical sources or to the current Media3 release notes.
3. Agree to the [Code of Conduct](CODE_OF_CONDUCT.md).
4. Open an Issue before you start coding non-trivial changes. Use the templates in `.github/ISSUE_TEMPLATE/`.

## Issue types

- **Bug:** a shipped skill has an incorrect API, an example that does not compile, or a claim that is false on the current Media3 release.
- **Feature:** a new skill, or a new major section inside an existing skill.
- **Docs:** a typo, unclear wording, or a broken link.
- **Question:** clarification on an API or workflow.

## Skill anatomy (non-negotiable)

Every skill `SKILL.md` **MUST** include these five blocks, in this order:

1. **YAML frontmatter** with `name`, `description` beginning with *"Use this skill to ..."*, `license`, `metadata.author`, `metadata.version`, `metadata.target_media3_version`, `metadata.last_reviewed`, and `keywords`.
2. **Prerequisites** with hard **MUST** and **MUST NOT** gates.
3. **Step 1: plan** describing what the agent should grep, enumerate, and flag before editing code.
4. **Numbered steps**, each step one atomic concept, with **RIGHT** and **WRONG** code pairs for every non-trivial pattern.
5. **Common pitfalls** grounded in real production bugs.

## Size budget

- Target **10,000 to 18,000 characters** in the `SKILL.md` body.
- Hard ceiling **20,000 characters**. CI enforces this. Move overflow to `references/*.md` companions and link with relative paths.

## Style

- **No em dashes.** Use commas, periods, or restructure.
- **No contractions.** Always write full forms ("is not", "do not", "cannot").
- Use **MUST**, **MUST NOT**, **DO NOT**, **PREFERRED** in bold.
- Every code block **MUST** be tagged with a language (`kotlin`, `xml`, `toml`, `bash`).

## Seven-phase authoring workflow

For a new skill:

1. **Scope** the workflow the skill owns.
2. **Source** authoritative references from the current Media3 release and `androidx/media` source.
3. **Validate** with a toy validation plan (minimal app, Media3 version, device API levels, pass/fail scenarios).
4. **Draft** the `SKILL.md` under `.skills/media/<skill-name>/SKILL.md`.
5. **Dry run** the skill against a real prompt. File any mismatches as Bug or Docs Issues.
6. **Review** by at least one peer reviewer on the PR.
7. **Publish** via squash merge to `main`.

## Pull request flow

1. Fork the repo, branch from `main`, name your branch `skill/<name>`, `fix/<short>`, or `docs/<short>`.
2. Follow the PR template in [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md).
3. CI **MUST** be green before review.
4. At least one approving review is required.
5. We squash-merge. The commit message will include the issue reference.

## Citation policy

When a skill cites an API, behavior, or version claim:

- Primary: Media3 release notes + `androidx/media` source.
- Secondary: developer.android.com guides.
- Tertiary: the reference repositories in [REFERENCES.md](REFERENCES.md).
- Community posts are never a primary citation in a skill's RIGHT path.

If a community source contradicts the release notes, trust the release notes and file a `type-docs` Issue against the contradicting source.

## Out of scope for v1.x

- `media3-low-latency-live`
- `media3-offline-downloads`
- `media3-transformer-editing`
- Any API marked `@ExperimentalApi` in the current Media3 release, including `CompositionPlayer`.

Tier 3 ideas become Feature Issues, not surprise skills.

## DCO and CLA

This project does not require a CLA. All contributions are accepted under the [Apache License 2.0](LICENSE) as per Section 5 of the license text.

Use `git commit -s` to sign off your commits if you want an auditable DCO-style record. This is optional today but may become required.

## Reviewing skills

Reviewers should verify:

- All **MUST** and **MUST NOT** statements are accurate against the current Media3 release.
- Every Kotlin code block compiles in a toy app at the pinned Media3 version.
- At least one **RIGHT** / **WRONG** pair per non-trivial step.
- Body is between 10k and 20k characters.
- `metadata.last_reviewed` is the date of the publish commit.
- No `@ExperimentalApi` class appears in any RIGHT example.
