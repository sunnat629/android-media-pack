---
name: media3-lifecycle-state
description: "Compact skill for lifecycle-aware MediaController, player state, saved position, process death, and release safety."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - lifecycle
    - state
    - mediacontroller
    - savedstatehandle
    - process-death
---

## Trigger

Use when player/controller leaks, recreation bugs, lifecycle pauses, saved position, or process-death recovery are involved.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Acquire/release controller exactly once per host lifecycle.
- Persist progress in repository/SavedStateHandle as appropriate.
- Gate video decoding by lifecycle and active page.
- Do not let every row collect the player directly.

## Example

```kotlin
DisposableEffect(sessionToken) {
    val future = MediaController.Builder(context, sessionToken).buildAsync()
    onDispose { MediaController.releaseFuture(future) }
}
```

## Related

- `media3-background-playback-service`
- `media3-compose-ui-material3`
- `streaming-media-architecture`
