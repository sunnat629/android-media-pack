---
name: media3-drm-widevine-setup
description: "Compact skill for Media3 Widevine DRM, license headers, offline licenses, L1/L3, HDCP, and failure recovery."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - drm
    - widevine
    - license
    - offline-license
    - hdcp
---

## Trigger

Use for protected playback, license URL/headers, offline keys, security-level gating, and DRM errors.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Put DRM config on `MediaItem` or media source factory path consistently.
- Do not log license tokens or PII.
- Gate HD/L1 content by actual device capability.
- Classify provisioning, license, key-expired, and output-protection failures separately.

## Example

```kotlin
MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
    .setLicenseUri(licenseUrl)
    .build()
```

## Related

- `media3-datasources-networking`
- `media3-analytics-telemetry`
