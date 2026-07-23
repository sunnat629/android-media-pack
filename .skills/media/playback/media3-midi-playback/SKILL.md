---
name: media3-midi-playback
description: "Compact skill for Media3 MIDI playback with dependency checks, decoder/runtime caveats, and Android playback integration."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - midi
    - audio
    - exoplayer
    - decoder
---

## Trigger

Use when an app must play MIDI files or add `media3-exoplayer-midi`.

## Rules

- Start with `media3-audio-playback` for audio focus, attributes, session, and noisy-device behavior.
- Pin Media3 to `1.10.1` through the version catalog.
- `media3-exoplayer-midi` depends on JSyn, which is published on JitPack. The project **MUST** add the `https://jitpack.io` Maven repository or dependency resolution fails.
- Read the current Media3 MIDI decoder notes before implementation; MIDI has extra dependency/runtime requirements. See <https://github.com/androidx/media/blob/release/libraries/decoder_midi/README.md>.
- Keep MIDI playback in Android source sets; expose playlist/state upward as KMP-safe data.
- Validate file source, duration, seeking behavior, and device support before promising parity with audio files.
- Treat MIDI as generated playback, not as ordinary decoded MP3/AAC content.

## Example

```toml
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-exoplayer-midi = { module = "androidx.media3:media3-exoplayer-midi", version.ref = "media3" }
```

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven(url = "https://jitpack.io")
    }
}
```

```kotlin
player.setMediaItem(MediaItem.fromUri(midiUri))
player.prepare()
```

## Do Not

- Do not skip the upstream MIDI README/dependency check.
- Do not assume MIDI supports every audio feature used by streamed media.
- Do not put MIDI-specific fallback logic in UI code.

## Related

- `media3-audio-playback`
- `media3-background-playback-service`
- `media3-analytics-telemetry`
