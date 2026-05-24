---
name: media3-audio-playback
description: "Compact skill for Media3 audio attributes, audio focus, becoming-noisy, chapters, metadata, and session controls."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - audio
    - audiofocus
    - becoming-noisy
    - metadata
    - mediasession
---

## Trigger

Use for music, podcast, audiobook, or background audio behavior.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Set AudioAttributes and handle audio focus.
- Enable becoming-noisy handling.
- Use MediaSessionService for background controls.
- Do not use media usage for alarms/navigation/ringtones.

## Example

```kotlin
exo.setAudioAttributes(audioAttributes, true)
exo.setHandleAudioBecomingNoisy(true)
```

## Related

- `media3-background-playback-service`
- `media3-analytics-telemetry`
