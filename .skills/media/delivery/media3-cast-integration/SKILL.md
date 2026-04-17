---
name: media3-cast-integration
description: Use this skill to integrate Google Cast with an Android app using AndroidX Media3 1.10.0. Use this skill to construct CastPlayer with CastPlayer.Builder(context).setLocalPlayer(exoPlayer), hand off playback between the local ExoPlayer and remote Cast receiver automatically, expose a MediaRouteButton from the media3-cast Compose surface, wire the Cast context via CastOptionsProvider, and handle session lifecycle events and common Cast failures.
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
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** include `media3-cast` and depend on Google Cast framework.
- Project **MUST** declare a `CastOptionsProvider` in the manifest.
- Project **MUST NOT** hand-swap `Player` instances when the user selects a Cast target.

## Step 1: plan

1. Confirm the app already has a `MediaSessionService` hosting the local `ExoPlayer`.
2. Register the receiver app ID.
3. Decide the UI entry point for `MediaRouteButton`.
4. Enumerate content types served.
5. Confirm Cast device discovery is not blocked by app-level network config.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"
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

```kotlin
import androidx.compose.runtime.Composable
import androidx.media3.cast.compose.MediaRouteButton

@Composable
fun TopBar() {
    MediaRouteButton()
}
```

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

- **Missing `OPTIONS_PROVIDER_CLASS_NAME`.**
- **Manual local-to-remote swap.**
- **Releasing the local `ExoPlayer` on Cast session start.**
- **Missing `setMimeType` on `MediaItem`.**
- **Custom receiver ID without custom options.**
- **Missing cleartext rule on HTTP streams.**
- **Running Cast on the Android Emulator expecting device discovery.**
