---
name: media3-workmanager-offline-ops
description: "Compact skill for Media3 WorkManager-backed offline operations, scheduling, retries, constraints, and foreground handoff."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - workmanager
    - offline
    - download
    - scheduling
---

## Trigger

Use for offline media work, scheduled downloads, retryable background operations, WorkManager integration, or `media3-exoplayer-workmanager`.

## Rules

- Start with `media3-background-playback-service` and `media3-datasources-networking` for service and network policy.
- Pin Media3 to `1.10.1` through the version catalog.
- Use WorkManager for deferrable, constraint-aware work; use a foreground service for active playback.
- Keep download/cache policy centralized and observable.
- Model constraints: network type, charging, storage, account entitlement, and retry backoff.
- Make cancellation and cleanup explicit; stale downloads are product bugs.
- Track download state separately from player state.

## Example

```toml
media3-exoplayer-workmanager = { module = "androidx.media3:media3-exoplayer-workmanager", version.ref = "media3" }
```

```kotlin
val request = OneTimeWorkRequestBuilder<OfflineMediaWorker>()
    .setConstraints(downloadConstraints)
    .build()
```

## Do Not

- Do not use WorkManager for live playback lifetime.
- Do not start unbounded parallel downloads.
- Do not let UI screens own offline queue truth.

## Related

- `media3-background-playback-service`
- `media3-datasources-networking`
- `media3-vod-playback`
