---
name: media3-test-utils-robolectric
description: "Compact skill for Media3 test utilities, Robolectric patterns, JVM boundaries, fake playback state, and realistic media assertions."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - testing
    - robolectric
    - test-utils
    - fake-player
---

## Trigger

Use for Media3 unit tests, Robolectric tests, fake player state, controller/service tests, or adding `media3-test-utils`.

## Rules

- Pin Media3 to `1.10.1` through the version catalog.
- Put JVM-only test helpers in `testImplementation`; put device/UI helpers in `androidTestImplementation`.
- Test app media state, ownership, and failure behavior; do not over-test Media3 internals.
- Prefer fake player/controller state for UI models and service boundaries.
- Use Robolectric for Android framework behavior that does not need real codec/network hardware.
- Use instrumented tests for decoder, DRM, renderer, surface, and real playback checks.
- Keep sample media tiny, deterministic, and license-safe.

## Example

```toml
media3-test-utils = { module = "androidx.media3:media3-test-utils", version.ref = "media3" }
media3-test-utils-robolectric = { module = "androidx.media3:media3-test-utils-robolectric", version.ref = "media3" }
```

```kotlin
assertThat(viewState.isPlaying).isFalse()
assertThat(viewState.errorMessage).isNull()
```

## Do Not

- Do not rely on remote media URLs in unit tests.
- Do not run real DRM or codec assumptions in plain JVM tests.
- Do not assert private Media3 implementation details.

## Related

- `media3-lifecycle-state`
- `media3-background-playback-service`
- `media3-analytics-telemetry`
