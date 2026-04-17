---
name: media3-drm-widevine-setup
description: Use this skill to set up Widevine DRM end to end in an Android app using AndroidX Media3 1.9.0. Use this skill to wire DefaultDrmSessionManagerProvider onto DefaultMediaSourceFactory, construct MediaItem.DrmConfiguration with C.WIDEVINE_UUID, pass license URL with custom HTTP headers, acquire and release offline licenses with OfflineLicenseHelper, detect Widevine L1 vs L3 security levels, handle HDCP policy gating, and recover from provisioning and license failure modes.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - drm
    - widevine
    - dash
    - hls
    - offline-license
    - hdcp
    - security-level
    - exoplayer

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later. Widevine APIs below that level are not supported by Media3.
- Project **MUST** pin Media3 to **1.9.0** or later.
- App **MUST** target streams packaged with Widevine CENC. ClearKey and PlayReady are out of scope for this skill.
- Project **MUST NOT** pass a `DrmSessionManager` directly to a `MediaSource.Factory`. Use `DefaultDrmSessionManagerProvider` on `DefaultMediaSourceFactory` instead.
- Project **MUST NOT** hardcode a license URL in the `MediaSource.Factory`. License URL belongs on the `MediaItem.DrmConfiguration`.
- The license server **MUST** accept the Widevine Modular license request format and return the key set.

## Step 1: plan

Before wiring DRM, enumerate the following:

1. Identify every `MediaSource.Factory` or `DefaultMediaSourceFactory` in the project. The DRM provider **MUST** be attached to the factory, not to individual sources.
2. Enumerate streams and group by license server. Each group gets one `MediaItem.DrmConfiguration` template.
3. Flag any place that stores raw license blobs. Offline licenses **MUST** be represented by an opaque `keySetId` byte array returned by `OfflineLicenseHelper`.
4. Flag any HDCP assumption in the UI (for example, "4K always allowed"). Device HDCP capability **MUST** be checked at runtime via `MediaDrm.getPropertyString("securityLevel")` and the `MediaDrm.HdcpLevel` constants.
5. Confirm all license requests go through HTTPS with a `User-Agent` that identifies the app. The license server operator needs that for abuse triage.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"

[libraries]
media3-exoplayer       = { module = "androidx.media3:media3-exoplayer",       version.ref = "media3" }
media3-exoplayer-dash  = { module = "androidx.media3:media3-exoplayer-dash",  version.ref = "media3" }
media3-exoplayer-hls   = { module = "androidx.media3:media3-exoplayer-hls",   version.ref = "media3" }
media3-datasource-okhttp = { module = "androidx.media3:media3-datasource-okhttp", version.ref = "media3" }
```

**DO NOT** add `exoplayer2` artifacts. Widevine wiring in the legacy package is source-incompatible.

## Step 3: construct a DefaultDrmSessionManagerProvider

### RIGHT

```kotlin
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

val httpFactory = DefaultHttpDataSource.Factory()
    .setUserAgent("WidevineToy/1.0 (Shunnek Labs)")
    .setConnectTimeoutMs(15_000)
    .setReadTimeoutMs(15_000)

val drmProvider = DefaultDrmSessionManagerProvider().apply {
    setDrmHttpDataSourceFactory(httpFactory)
}

val mediaSourceFactory = DefaultMediaSourceFactory(context)
    .setDataSourceFactory(httpFactory)
    .setDrmSessionManagerProvider(drmProvider)

val player = ExoPlayer.Builder(context)
    .setMediaSourceFactory(mediaSourceFactory)
    .build()
```

### WRONG

```kotlin
// WRONG: passing DrmSessionManager directly into a MediaSource.Factory is the removed legacy pattern
val drmSessionManager = DefaultDrmSessionManager.Builder().build(httpMediaDrmCallback)
val factory = DashMediaSource.Factory(dataSourceFactory)
    .setDrmSessionManager(drmSessionManager)
```

## Step 4: attach the license URL per MediaItem

### RIGHT

```kotlin
import androidx.media3.common.C
import androidx.media3.common.MediaItem

val widevineItem = MediaItem.Builder()
    .setUri("https://example.com/stream.mpd")
    .setMimeType("application/dash+xml")
    .setDrmConfiguration(
        MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
            .setLicenseUri("https://example.com/widevine/license")
            .setLicenseRequestHeaders(mapOf("Authorization" to "Bearer $token"))
            .setMultiSession(false)
            .build()
    )
    .build()

player.setMediaItem(widevineItem)
player.prepare()
player.playWhenReady = true
```

### WRONG

```kotlin
// WRONG: hardcoding license URL on the factory prevents per-user tokens and per-item license rotation
val drmProvider = DefaultDrmSessionManagerProvider().apply {
    setDrmHttpDataSourceFactory(httpFactory)
    setDrmUuid(C.WIDEVINE_UUID)
    setLicenseUri("https://example.com/widevine/license") // reference-only signature, do not share across users
}
```

## Step 5: detect Widevine security level

### RIGHT

```kotlin
import android.media.MediaDrm
import androidx.media3.common.C

enum class WidevineLevel { L1, L2, L3, UNKNOWN }

fun detectWidevineLevel(): WidevineLevel {
    return try {
        MediaDrm(C.WIDEVINE_UUID).use { drm ->
            when (drm.getPropertyString("securityLevel")) {
                "L1" -> WidevineLevel.L1
                "L2" -> WidevineLevel.L2
                "L3" -> WidevineLevel.L3
                else -> WidevineLevel.UNKNOWN
            }
        }
    } catch (unsupported: android.media.UnsupportedSchemeException) {
        WidevineLevel.UNKNOWN
    }
}
```

Use the result to filter adaptive variants. On devices reporting L3, the app **MUST NOT** request variants flagged by the license server as L1-only.

### WRONG

```kotlin
// WRONG: assuming every device at Android 7+ supports L1 overestimates capability on emulators and rooted devices
val supportsL1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
```

## Step 6: acquire and release offline licenses

Offline licenses are represented by an opaque `keySetId` byte array. Persist it alongside the media item, pass it back into the `MediaItem.DrmConfiguration` for offline playback, and call `releaseLicense` when the content is removed.

### RIGHT

```kotlin
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.OfflineLicenseHelper

suspend fun acquireOfflineLicense(format: Format): ByteArray {
    val helper = OfflineLicenseHelper.newWidevineInstance(
        /* defaultLicenseUrl = */ "https://example.com/widevine/license",
        /* forceDefaultLicenseUrl = */ false,
        DefaultHttpDataSource.Factory(),
        /* optionalKeyRequestParameters = */ mapOf("Authorization" to "Bearer $token"),
        androidx.media3.exoplayer.drm.DrmSessionEventListener.EventDispatcher(),
    )
    return try {
        helper.downloadLicense(format)
    } finally {
        helper.release()
    }
}
```

Pass the `keySetId` back when constructing the offline `MediaItem`:

```kotlin
MediaItem.Builder()
    .setUri(localUri)
    .setDrmConfiguration(
        MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
            .setKeySetId(keySetId)
            .build()
    )
    .build()
```

**DO NOT** forget to call `OfflineLicenseHelper.releaseLicense(keySetId)` when the user removes the download. Leaked offline licenses count against the device concurrent session limit.

## Step 7: handle DRM errors

### RIGHT

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED -> retryProvisioningWithBackoff()
            PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> requestFreshToken()
            PlaybackException.ERROR_CODE_DRM_LICENSE_EXPIRED -> refreshOfflineLicense()
            PlaybackException.ERROR_CODE_DRM_CONTENT_ERROR -> reportUnplayableStream()
            PlaybackException.ERROR_CODE_DRM_DISALLOWED_OPERATION -> showHdcpRequiredMessage()
            else -> analytics.logPlayerError(error)
        }
    }
})
```

### WRONG

```kotlin
// WRONG: generic retry for any DRM error burns license-server quota and hides HDCP gating
player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        if (error.message.orEmpty().contains("DRM", ignoreCase = true)) player.prepare()
    }
})
```

## Step 8: respect HDCP policy

License policy can require an HDCP level from the output. On a mirrored or unencrypted external display, Widevine blocks decode. Surface this instead of showing a frozen frame.

### RIGHT

```kotlin
override fun onPlayerError(error: PlaybackException) {
    if (error.errorCode == PlaybackException.ERROR_CODE_DRM_DISALLOWED_OPERATION) {
        ui.showToast("This video requires a secure HDMI connection.")
    }
}
```

**DO NOT** attempt to force-decode HDCP-restricted content with a secondary software path. Doing so violates the license policy and the device attestation.

## Step 9: license request headers and authentication

License servers almost always require an auth token, device fingerprint, or both. Pass these through `setLicenseRequestHeaders`.

### RIGHT

```kotlin
MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
    .setLicenseUri("https://example.com/widevine/license")
    .setLicenseRequestHeaders(
        mapOf(
            "Authorization" to "Bearer $userJwt",
            "X-Device-Id" to deviceId,
            "X-App-Version" to appVersion,
        )
    )
    .build()
```

**DO NOT** embed the auth token in the URL query string. License URLs are logged by upstream proxies and CDNs.

## Common pitfalls

- **Provider attached to the wrong place.** `DefaultDrmSessionManagerProvider` **MUST** be attached to `DefaultMediaSourceFactory`, never to individual `MediaSource.Factory` instances.
- **Missing `setLicenseRequestHeaders`.** Auth tokens **MUST** be sent as request headers, not as URL query parameters.
- **L1-only policy on L3 devices.** Always detect `securityLevel` before offering high-resolution variants. Otherwise the license server returns `ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED` at stream start.
- **Leaked offline licenses.** Every successful `downloadLicense` **MUST** have a matching `releaseLicense` when the content is removed.
- **Silent retry of provisioning.** Exponential backoff is required. A tight retry loop can blacklist the device with the license server.
- **Forcing playback on HDCP-restricted output.** `ERROR_CODE_DRM_DISALLOWED_OPERATION` exists to surface this. Respect it.
- **Clock skew.** If the device clock is more than a few minutes off, the license often fails to validate. Surface a clear message.
- **Using MediaSessionCompat.** Widevine flow does not require MediaSessionCompat. Use Media3 `MediaSession` if background playback is also required.
