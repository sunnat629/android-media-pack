# Security Policy

## Supported versions

The pack is aligned with **AndroidX Media3 1.10.1** and receives fixes on the current release series only. Older major versions of the pack will receive critical security fixes on a best-effort basis.

| Pack version | Media3 pin | Supported |
|---|---|---|
| 1.x | 1.9.0 | ✅ |
| 0.x | pre-1.9.0 | ❌ |

## Reporting a vulnerability

Email **suncha629@gmail.com** with the subject line `[security] android-media-skill`. Include:

- A clear description of the issue and the affected skill or file.
- Reproduction steps or a proof of concept.
- The impact you observed.
- Your preferred attribution (optional).

We aim to respond within **3 business days** and to land a fix or a mitigation within **30 days** for high-severity issues.

Do **NOT** file public GitHub Issues for undisclosed vulnerabilities. Use the email address above.

## What counts as a vulnerability for this pack

The pack ships documentation-only skills. "Vulnerabilities" here are typically:

- A skill recommends a pattern that leaks secrets (for example, auth tokens in URLs, license keys written to logs).
- A skill recommends a pattern that bypasses Widevine or HDCP policy.
- A skill contains sample code with an obvious injection, path-traversal, or SSRF issue.
- A CI workflow on this repository is misconfigured in a way that exposes the repository to supply chain risk.

Upstream Media3 bugs should be reported to [androidx/media](https://github.com/androidx/media/issues).
