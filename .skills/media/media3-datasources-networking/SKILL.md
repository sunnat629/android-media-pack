---
name: media3-datasources-networking
description: Use this skill to choose and wire the right network stack for AndroidX Media3 1.9.0 playback. Use this skill to pick between DefaultHttpDataSource, OkHttpDataSource, CronetDataSource, and HttpEngineDataSource (Android 14+), attach a shared DataSource.Factory to DefaultMediaSourceFactory and DefaultDrmSessionManagerProvider, wire SimpleCache with CacheDataSource for offline-first playback, and inject custom HTTP headers, User-Agent, timeouts, and interceptors.
license: Complete terms in LICENSE.txt
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.9.0"
  last_reviewed: "2026-04-16"
  keywords:
    - android
    - media3
    - datasource
    - okhttp
    - cronet
    - httpengine
    - simplecache
    - cachedatasource
    - networking

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.9.0** or later.
- Project **MUST** use HTTPS for all media URLs. Cleartext is not covered by this skill.
- Project **MUST NOT** construct two different `DataSource.Factory` instances for the same stream that diverge in headers, User-Agent, or TLS configuration. DRM, manifest, and segment fetches **MUST** share one factory.
- Project **MUST NOT** build `OkHttpClient` without a timeout. Media3 inherits whatever timeouts the supplied client has.

## Step 1: plan

1. Enumerate every current use of `DefaultHttpDataSource`, `OkHttpDataSource`, or a hand-rolled `HttpDataSource`. Target a single choice for the app.
2. Decide the network stack by platform tier:
    - API 34+: **PREFERRED** is `HttpEngineDataSource` (Cronet-backed by the platform).
    - API 21 to 33: **PREFERRED** is `OkHttpDataSource` on an `OkHttpClient` you already own. Falls back to `DefaultHttpDataSource` for minimal apps.
    - App already uses Cronet directly: keep `CronetDataSource`.
3. Decide the cache policy:
    - No offline, short sessions: no cache, pure `HttpDataSource.Factory`.
    - Resume-across-launches: `SimpleCache` + `CacheDataSource`.
    - Downloads: see the future `media3-offline-downloads` skill (out of scope here).
4. Identify auth and headers. License server, manifest, and segments often need different headers.
5. Confirm the same factory is wired into both `DefaultMediaSourceFactory.setDataSourceFactory(...)` and `DefaultDrmSessionManagerProvider.setDrmHttpDataSourceFactory(...)`.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"
okhttp = "4.12.0"

[libraries]
media3-exoplayer       = { module = "androidx.media3:media3-exoplayer",       version.ref = "media3" }
media3-datasource-okhttp = { module = "androidx.media3:media3-datasource-okhttp", version.ref = "media3" }
media3-datasource-cronet = { module = "androidx.media3:media3-datasource-cronet", version.ref = "media3" }
okhttp                 = { module = "com.squareup.okhttp3:okhttp",             version.ref = "okhttp" }
```

Pick only what the app uses. Adding both okhttp and cronet doubles the APK size.

## Step 3: OkHttp-backed DataSource (API 21 to 33)

### RIGHT

```kotlin
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val okHttp = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .callTimeout(45, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

val dataSourceFactory = OkHttpDataSource.Factory(okHttp)
    .setUserAgent("Shunnek/1.0 (Android)")
    .setDefaultRequestProperties(mapOf("X-App-Version" to appVersion))
```

### WRONG

```kotlin
// WRONG: building OkHttpClient() with no timeouts lets the client hang indefinitely,
// which freezes manifest fetches and produces "playback stuck" bug reports
val okHttp = OkHttpClient()
```

## Step 4: HttpEngineDataSource (API 34+)

### RIGHT

```kotlin
import android.net.http.HttpEngine
import androidx.media3.datasource.HttpEngineDataSource

val httpEngine = HttpEngine.Builder(context)
    .setEnableHttp2(true)
    .setEnableQuic(true)
    .build()

val dataSourceFactory = HttpEngineDataSource.Factory(httpEngine)
    .setUserAgent("Shunnek/1.0 (Android)")
    .setDefaultRequestProperties(mapOf("X-App-Version" to appVersion))
```

`HttpEngineDataSource` uses the platform HTTP engine on supported devices and transparently adds HTTP/2 and HTTP/3 over QUIC.

### WRONG

```kotlin
// WRONG: wrapping HttpEngine inside an OkHttpClient re-implements what the platform already does
val okHttp = OkHttpClient.Builder().addInterceptor(HttpEngineInterceptor(httpEngine)).build()
```

## Step 5: share a single factory across player and DRM

```kotlin
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

val drmProvider = DefaultDrmSessionManagerProvider().apply {
    setDrmHttpDataSourceFactory(dataSourceFactory)
}

val mediaSourceFactory = DefaultMediaSourceFactory(context)
    .setDataSourceFactory(dataSourceFactory)
    .setDrmSessionManagerProvider(drmProvider)

val player = ExoPlayer.Builder(context)
    .setMediaSourceFactory(mediaSourceFactory)
    .build()
```

**DO NOT** build a separate `DefaultHttpDataSource` for DRM. That diverges User-Agent and auth tokens from the media fetch.

## Step 6: SimpleCache + CacheDataSource

Use this for resume-across-launches behavior where the user expects partial content to persist.

### RIGHT

```kotlin
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

val cacheDir = File(context.cacheDir, "media3-cache")
val cacheEvictor = LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024)
val databaseProvider = StandaloneDatabaseProvider(context)
val cache = SimpleCache(cacheDir, cacheEvictor, databaseProvider)

val cacheSourceFactory = CacheDataSource.Factory()
    .setCache(cache)
    .setUpstreamDataSourceFactory(dataSourceFactory)
    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

val mediaSourceFactory = DefaultMediaSourceFactory(context)
    .setDataSourceFactory(cacheSourceFactory)
```

### WRONG

```kotlin
// WRONG: one SimpleCache per activity corrupts the cache on process recreation
class MainActivity : ComponentActivity() {
    private val cache = SimpleCache(cacheDir, evictor, StandaloneDatabaseProvider(this))
}
```

`SimpleCache` **MUST** be a process-wide singleton. Typically in `Application.onCreate` or a DI provider.

## Step 7: custom headers per request

When the license endpoint needs different headers than segments, attach headers to each `MediaItem` or `DrmConfiguration` rather than to the factory.

```kotlin
import androidx.media3.common.C
import androidx.media3.common.MediaItem

val item = MediaItem.Builder()
    .setUri("https://example.com/manifest.mpd")
    .setRequestMetadata(
        MediaItem.RequestMetadata.Builder()
            .setExtras(android.os.Bundle().apply {
                putString("Authorization", "Bearer $userJwt")
            })
            .build()
    )
    .build()
```

For Widevine license requests, attach to `DrmConfiguration.setLicenseRequestHeaders`. See the `media3-drm-widevine-setup` skill.

## Step 8: observe network errors

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> retryWithBackoff()
            PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED -> reportMisconfiguredUrl()
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> reportServerError(error)
            else -> analytics.logPlayerError(error)
        }
    }
})
```

## Step 9: release the cache on app teardown

`SimpleCache` holds file handles. Release it when the process is being torn down.

```kotlin
class App : Application() {
    lateinit var cache: SimpleCache

    override fun onCreate() {
        super.onCreate()
        cache = SimpleCache(...)
    }
}
```

**DO NOT** call `cache.release()` in an activity `onDestroy`. It invalidates the cache for the remaining activity stack.

## Common pitfalls

- **Multiple `DataSource.Factory` instances.** Diverges User-Agent, auth headers, and TLS config across fetches.
- **Separate factory for DRM.** DRM requests bypass OkHttp interceptors or Cronet features.
- **`OkHttpClient()` with no timeouts.** Hangs indefinitely.
- **`SimpleCache` per activity.** Corrupts the cache on rotation or process recreation.
- **Embedding auth tokens in URL query strings.** URLs are logged by CDNs and proxies.
- **Mixing HttpEngine and OkHttp in the same factory chain.** The platform engine already does what most interceptors would.
- **Not handling `ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED`.** Surfaces as a generic IO error to the user.
- **Cleartext `http://` media URLs on modern Android.** Blocked by default.
