---
name: media3-workmanager-offline-ops
description: "Compact skill for Media3 offline downloads with DownloadService, DownloadManager, Requirements, and the WorkManagerScheduler role of media3-exoplayer-workmanager."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
  keywords:
    - android
    - media3
    - workmanager
    - offline
    - download
    - downloadservice
    - downloadmanager
    - requirements
    - scheduling
---

## Trigger

Use for offline media downloads, download constraints and scheduling, retryable background media operations, or `media3-exoplayer-workmanager`.

## Rules

- Start with `media3-background-playback-service` and `media3-datasources-networking` for service and network policy.
- Pin Media3 to `1.10.1` through the version catalog.
- Offline downloads **MUST** use the official Media3 stack: a `DownloadService` subclass driven by a singleton `DownloadManager`, with downloads submitted as `DownloadRequest` via `DownloadService.sendAddDownload`. Use `DownloadHelper` to prepare adaptive (HLS/DASH) downloads and select tracks.
- `DownloadIndex` (from `DownloadManager`) is the single source of truth for download state. Track it separately from player state.
- Express constraints (network type, charging, storage) with Media3 `Requirements` applied through `DownloadService.sendSetRequirements`, **NOT** WorkManager `Constraints`.
- `media3-exoplayer-workmanager` only provides `WorkManagerScheduler`, a `Scheduler` that restarts the `DownloadService` when pending `Requirements` are met again. Return it from `getScheduler()`; that is the entire WorkManager role in downloads.
- Use WorkManager directly only for genuinely custom non-download deferrable work, for example cache cleanup or license refresh sweeps.
- `DownloadService` **MUST** be declared with `foregroundServiceType="dataSync"`, which on targetSdk 35+ has a 6-hour runtime cap per app session. Keep downloads resumable; the `Scheduler` restarts the service later.
- Keep download and cache policy centralized and observable.
- Make cancellation and removal explicit through `DownloadService.sendRemoveDownload`; stale downloads are product bugs.

## Example

```toml
media3-exoplayer-workmanager = { module = "androidx.media3:media3-exoplayer-workmanager", version.ref = "media3" }
```

```kotlin
class MediaDownloadService : DownloadService(NOTIFICATION_ID) {
    override fun getDownloadManager(): DownloadManager = AppDeps.downloadManager
    override fun getScheduler(): Scheduler = WorkManagerScheduler(this, "media3_downloads")
    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification = AppDeps.downloadNotificationHelper.buildProgressNotification(
        this, R.drawable.ic_download, null, null, downloads, notMetRequirements)
}

DownloadService.sendAddDownload(
    context, MediaDownloadService::class.java,
    DownloadRequest.Builder(contentId, contentUri).build(), false)

DownloadService.sendSetRequirements(
    context, MediaDownloadService::class.java,
    Requirements(Requirements.NETWORK_UNMETERED), false)
```

```xml
<service
    android:name=".download.MediaDownloadService"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

## Do Not

- **DO NOT** hand-roll download Workers; WorkManager cannot own Media3 download state or resumption.
- **DO NOT** gate downloads with WorkManager `Constraints`; use `Requirements`.
- Do not use WorkManager for live playback lifetime.
- Do not start unbounded parallel downloads; cap via `DownloadManager.maxParallelDownloads`.
- Do not let UI screens own offline queue truth; read `DownloadIndex`.

## Related

- `media3-background-playback-service`
- `media3-datasources-networking`
- `media3-vod-playback`
