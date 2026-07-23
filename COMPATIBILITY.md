# Compatibility

The pack pins against **Media3 1.10.1** and the Android toolchain below. Skills stay valid against this matrix until the pack bumps to a new Media3 release.

## Supported toolchain

| Component | Version |
| --- | --- |
| **Media3** | 1.10.1 |
| **minSdk** | 21 (Android 5.0) |
| **compileSdk / targetSdk** | 35 (Android 15) recommended, 34 minimum |
| **AGP** | 8.0 or later |
| **Kotlin** | 1.9 or later (2.0+ **PREFERRED** for Compose Material3 1.4+) |
| **Gradle** | 8.5 or later |
| **JDK** | 17 |
| **Compose BOM** | 2025.11.00 or later |
| **Material3** | 1.4.0 or later |
| **media3-ui-compose-material3** | 1.10.1 or later |

## Device matrix

Skills are verified against:

| API | Android | Notes |
| ----- | --------- | ------- |
| 21 | 5.0 | `minSdk` floor |
| 28 | 9 | Pre-FGS-type baseline |
| 33 | 13 | `POST_NOTIFICATIONS` runtime permission gate |
| 34 | 14 | User-initiated foreground service rules, `foregroundServiceType` enforcement |
| 35 | 15 | Latest default-on wake lock, Material3 dynamic color |

## Out of scope for v2.x

- Low-latency live (LL-HLS, LL-DASH)
- WebRTC / WHIP integration (tracked as `not_planned`)
- Any API marked `@ExperimentalApi` in Media3 1.10.1, including `CompositionPlayer`.

Transformer editing (`media3-transformer-editing`) and WorkManager-backed offline operations (`media3-workmanager-offline-ops`) shipped in 2.0.0 and are no longer out of scope.

## Upgrade policy

When a new Media3 release ships:

1. File a `type-feature` Issue titled `Audit pack for Media3 x.y.z`.
2. Re-review every skill whose `metadata.last_reviewed` is older than 90 days.
3. Bump `metadata.target_media3_version` and `metadata.last_reviewed` only after peer review sign-off.
4. Update the pack → Media3 version matrix in [REFERENCES.md](REFERENCES.md).
