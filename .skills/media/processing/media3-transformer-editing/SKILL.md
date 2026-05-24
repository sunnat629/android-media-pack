---
name: media3-transformer-editing
description: "Compact skill for Media3 Transformer editing and export workflows including trim, transcode, progress, cancellation, and errors."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - transformer
    - editing
    - transcode
    - export
---

## Trigger

Use for media editing/export flows: trim, transcode, clip export, composition, progress, cancellation, or `media3-transformer`.

## Rules

- Do not mix editing/export ownership with playback ownership.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Transformer code in Android source sets; expose KMP-safe job state upward.
- Model export as a job with input URI, output path, progress, cancellation, failure reason, and cleanup.
- Check codec/container support before promising a transformation.
- Run long exports outside composables and survive Activity recreation.
- Store only safe, scoped output URIs; respect Android storage rules.

## Example

```toml
media3-transformer = { module = "androidx.media3:media3-transformer", version.ref = "media3" }
```

```kotlin
data class ExportState(
    val progress: Float,
    val isRunning: Boolean,
    val error: String?
)
```

## Do Not

- Do not block the UI thread during export.
- Do not assume every input codec can be exported on every device.
- Do not leave partial output files after cancellation or failure.

## Related

- `media3-video-effects-lottie-muxer`
- `media3-inspector-metadata-thumbnails`
- `media3-workmanager-offline-ops`
