---
name: media3-live-only-streaming
description: "Compact skill for non-DVR Media3 live streams where seeking and resume do not exist."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - live-only
    - no-dvr
    - liveconfiguration
    - reconnect
---

## Trigger

Use when a live stream has no seekable DVR window and should always return to live edge.

## Rules

- Start with `streaming-media-architecture` for ownership, KMP split, preload window, and telemetry.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep Media3 APIs in Android source sets; expose KMP-safe state/events upward.
- Adopt `media3-live-streaming` first.
- No scrubber, chapters, rewind, or resume storage.
- Pause/resume snaps to live edge if product requires real-time behavior.
- Behind-live-window recovery is reload/snap-to-live.

## Example

```kotlin
val canSeek = player.isCurrentMediaItemSeekable
// live-only UI hides scrubber when !canSeek
```

## Related

- `media3-live-streaming`
- `media3-hls-dash-adaptive-streaming`
