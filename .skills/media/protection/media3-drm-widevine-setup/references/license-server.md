# License server: headers, tokens, and rotation

## Request headers

License servers almost always require an auth token, device fingerprint, or both. Pass these through `MediaItem.DrmConfiguration.Builder#setLicenseRequestHeaders`.

```kotlin
import androidx.media3.common.C
import androidx.media3.common.MediaItem

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

## Rules

- **DO NOT** embed the auth token in the URL query string. License URLs are logged by upstream proxies and CDNs.
- **DO NOT** reuse one token across users. Scope each token to the authenticated user and the session.
- **MUST** rotate tokens before they expire. Expired tokens surface as `ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED`.
- **PREFERRED** is short-lived JWTs (minutes to hours) over long-lived opaque tokens.
- **MUST** send the `User-Agent` in the data-source factory (`DefaultHttpDataSource.Factory#setUserAgent`). License server operators rely on it for abuse triage.

## Per-item license URLs

Per-item license URLs let you run A/B routing, regional license servers, and staged rollouts without restarting the player. Attach a fresh `MediaItem.DrmConfiguration` to each `MediaItem` rather than pinning one URL on `DefaultDrmSessionManagerProvider`.

## Multi-session

`setMultiSession(true)` enables multiple concurrent license sessions on the same `ExoPlayer`. Use it only when mixing Widevine streams that require distinct key sets (for example, ad + content with different policies). Otherwise keep it `false` to minimize license-server load.
