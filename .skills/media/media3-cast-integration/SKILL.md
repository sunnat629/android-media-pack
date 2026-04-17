---
name: media3-cast-integration
description: Use this skill to integrate Google Cast with an Android app using AndroidX Media3 1.9.0. Use this skill to construct CastPlayer with CastPlayer.Builder(context).setLocalPlayer(exoPlayer), hand off playback between the local ExoPlayer and remote Cast receiver automatically, expose a MediaRouteButton from the media3-cast Compose surface, wire the Cast context via CastOptionsProvider, and handle session lifecycle events and common Cast failures.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - cast
    - chromecast
    - castplayer
    - mediaroutebutton
    - castoptionsprovider
    - compose

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.9.0** or later. Earlier releases require manual local-to-remote `Player` swapping.
- Project **MUST** include `media3-cast` and depend on Google Cast framework (`com.google.android.gms:play-services-cast-framework`).
- Project **MUST** declare a `CastOptionsProvider` in the manifest.
- Project **MUST NOT** hand-swap `Player` instances when the user selects a Cast target. That logic now lives inside `CastPlayer`.

## Step 1: plan

1. Confirm the app already has a `MediaSessionService` hosting the local `ExoPlayer`. Cast integration plugs into that service.
2. Register the receiver app ID (default `CC1AD845` for styled media, or the custom receiver ID).
3. Decide the UI entry point for `MediaRouteButton`: top bar in Compose, `MediaRouteButton` Android view in XML, or both.
4. Enumerate content types served. The default Cast receiver supports HLS, DASH, MP4, and progressive audio. Custom receivers may require different MIME mapping.
5. Confirm Cast device discovery is not blocked by app-level network config. `cleartextTrafficPermitted` false is fine, Cast uses HTTPS.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"
play-services-cast = "22.0.0"

[libraries]
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-session   = { module = "androidx.media3:media3-session",   version.ref = "media3" }
media3-cast      = { module = "androidx.media3:media3-cast",      version.ref = "media3" }
play-services-cast-framework = { module = "com.google.android.gms:play-services-cast-framework", version.ref = "play-services-cast" }
```

## Step 3: provide CastOptions

### RIGHT

```kotlin
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
```

### Manifest

```xml
<application>
    <meta-data
        android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
        android:value="com.example.cast.CastOptionsProvider" />
</application>
```

**DO NOT** skip the meta-data entry. Without it, `CastContext.getSharedInstance()` throws `IllegalStateException`.

## Step 4: construct CastPlayer with setLocalPlayer

### RIGHT

```kotlin
import androidx.media3.cast.CastPlayer
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

val exoPlayer = ExoPlayer.Builder(context).build()
val castPlayer = CastPlayer.Builder(context)
    .setLocalPlayer(exoPlayer)
    .build()

val session = MediaSession.Builder(context, castPlayer).build()
```

`CastPlayer` transitions between `exoPlayer` and the remote Cast receiver automatically when the user selects or disconnects a Cast target. The `MediaSession` sees a single `Player` instance.

### WRONG

```kotlin
// WRONG: manual swap bypasses CastPlayer and breaks state transfer
fun onCastSessionStarted() {
    currentPlayer = remoteCastPlayer
    session.setPlayer(remoteCastPlayer)
}
fun onCastSessionEnded() {
    currentPlayer = exoPlayer
    session.setPlayer(exoPlayer)
}
```

## Step 5: expose MediaRouteButton in Compose

### RIGHT

```kotlin
import androidx.compose.runtime.Composable
import androidx.media3.cast.compose.MediaRouteButton

@Composable
fun TopBar() {
    MediaRouteButton()
}
```

For XML toolbars, use the Android `MediaRouteButton` view from `androidx.mediarouter:mediarouter`.

## Step 6: listen for session lifecycle

```kotlin
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener

castPlayer.setSessionAvailabilityListener(object : SessionAvailabilityListener {
    override fun onCastSessionAvailable() {
        analytics.logCastStarted()
    }
    override fun onCastSessionUnavailable() {
        analytics.logCastEnded()
    }
})
```

**DO NOT** release the local `ExoPlayer` on `onCastSessionAvailable`. `CastPlayer` is still delegating to it for local fallback. Release only when the whole playback flow ends.

## Step 7: handle Cast errors

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

castPlayer.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_REMOTE_ERROR -> ui.showReceiverError()
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> ui.showNetworkError()
            else -> analytics.logPlayerError(error)
        }
    }
})
```

## Step 8: content type handoff

The default Cast receiver accepts HLS, DASH, MP4, and common audio codecs. The `MediaItem` handed to `CastPlayer.setMediaItems(...)` **MUST** carry an accurate `setMimeType`. **DO NOT** rely on URL suffix sniffing on the receiver side.

```kotlin
import androidx.media3.common.MimeTypes
import androidx.media3.common.MediaItem

val item = MediaItem.Builder()
    .setUri("https://example.com/master.m3u8")
    .setMimeType(MimeTypes.APPLICATION_M3U8)
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle("Example Stream")
            .setArtist("Shunnek Labs")
            .build()
    )
    .build()
castPlayer.setMediaItems(listOf(item))
castPlayer.prepare()
```

## Common pitfalls

- **Missing `OPTIONS_PROVIDER_CLASS_NAME`.** `CastContext.getSharedInstance()` throws without it.
- **Manual local-to-remote swap.** The 1.9.0 `CastPlayer.setLocalPlayer` pattern handles this automatically.
- **Releasing the local `ExoPlayer` on Cast session start.** Breaks local fallback.
- **Missing `setMimeType` on `MediaItem`.** Default receiver cannot resolve unlabeled streams.
- **Custom receiver ID without custom options.** The default-receiver styling must also be swapped.
- **Missing cleartext rule on HTTP streams.** Cast receivers require HTTPS for media URLs on modern Android.
- **Running Cast on the Android Emulator expecting device discovery.** The emulator cannot discover Cast targets on most hosts.
