# Offline licenses

Offline licenses are represented by an opaque `keySetId` byte array. Persist it alongside the downloaded media item, pass it back into the `MediaItem.DrmConfiguration` for offline playback, and call `releaseLicense` when the content is removed.

## Acquire

```kotlin
import androidx.media3.common.Format
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.DrmSessionEventListener
import androidx.media3.exoplayer.drm.OfflineLicenseHelper

suspend fun acquireOfflineLicense(format: Format, token: String): ByteArray {
    val helper = OfflineLicenseHelper.newWidevineInstance(
        /* defaultLicenseUrl = */ "https://example.com/widevine/license",
        /* forceDefaultLicenseUrl = */ false,
        DefaultHttpDataSource.Factory(),
        /* optionalKeyRequestParameters = */ mapOf("Authorization" to "Bearer $token"),
        DrmSessionEventListener.EventDispatcher(),
    )
    return try {
        helper.downloadLicense(format)
    } finally {
        helper.release()
    }
}
```

## Replay

```kotlin
import androidx.media3.common.C
import androidx.media3.common.MediaItem

fun offlineMediaItem(localUri: String, keySetId: ByteArray): MediaItem =
    MediaItem.Builder()
        .setUri(localUri)
        .setDrmConfiguration(
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setKeySetId(keySetId)
                .build()
        )
        .build()
```

## Release

```kotlin
suspend fun releaseOfflineLicense(keySetId: ByteArray, token: String) {
    val helper = OfflineLicenseHelper.newWidevineInstance(
        "https://example.com/widevine/license",
        false,
        DefaultHttpDataSource.Factory(),
        mapOf("Authorization" to "Bearer $token"),
        DrmSessionEventListener.EventDispatcher(),
    )
    try {
        helper.releaseLicense(keySetId)
    } finally {
        helper.release()
    }
}
```

## Rules

- **MUST** persist `keySetId` transactionally with the downloaded content manifest.
- **MUST** release the license on content deletion. Leaked offline licenses count against the device concurrent session limit.
- **MUST** handle `ERROR_CODE_DRM_LICENSE_EXPIRED` by refreshing: call `downloadLicense` again with the same format and overwrite the stored `keySetId`.
- **DO NOT** store raw license blobs. `keySetId` is the only handle.
- **DO NOT** share `keySetId` across devices. Each license is bound to the device provisioning.
