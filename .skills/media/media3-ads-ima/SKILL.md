---
name: media3-ads-ima
description: Use this skill to integrate Google IMA ads with AndroidX Media3 1.9.0, covering both client-side (CSAI) and server-side (SSAI / DAI) ad insertion. Use this skill to wire ImaAdsLoader into DefaultMediaSourceFactory, build ImaServerSideAdInsertionMediaSource for DAI streams, supply an AdViewProvider from a PlayerView or Compose host, handle companion ad slots, and distinguish ad playback errors from content errors.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - ima
    - ads
    - csai
    - ssai
    - dai
    - imaadsloader
    - adviewprovider
    - companion-ads

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.9.0** or later.
- Project **MUST** include `media3-exoplayer-ima` and depend on the Google IMA SDK for Android.
- Project **MUST NOT** implement a separate ad player window on top of the content. Media3 integrates ad scheduling into the main `Player` timeline.
- CSAI and SSAI are treated as two separate flows. **DO NOT** mix both in the same playlist.

## Step 1: plan

1. Decide CSAI or SSAI:
    - **CSAI** (client-side): app requests ads from an ad server via IMA. More control, more ad-blocker exposure.
    - **SSAI** (DAI): ads are stitched into the content stream server-side. Fewer ad-blocker issues, less client control.
2. Identify where the `AdViewProvider` comes from:
    - XML: `PlayerView.adViewGroup`.
    - Compose: custom `ViewGroup` emitted alongside the Compose `PlayerSurface`.
3. Decide companion ad slots up front. Retro-fitting companions after launch is expensive.
4. Confirm the ad tag URL is reachable with the app's User-Agent. IMA ad servers often filter.
5. Plan analytics separately. IMA emits its own events, Media3 emits playback events. Correlate by session and ad position.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"
ima = "3.34.0"

[libraries]
media3-exoplayer-ima = { module = "androidx.media3:media3-exoplayer-ima", version.ref = "media3" }
ima-android-sdk     = { module = "com.google.ads.interactivemedia.v3:interactivemedia", version.ref = "ima" }
```

## Step 3: CSAI with ImaAdsLoader

### RIGHT

```kotlin
import androidx.media3.common.AdViewProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

val adsLoader = ImaAdsLoader.Builder(context).build()

val mediaSourceFactory = DefaultMediaSourceFactory(context)
    .setDataSourceFactory(httpDataSourceFactory)
    .setLocalAdInsertionComponents({ adsLoader }, playerView /* AdViewProvider */)

val player = ExoPlayer.Builder(context)
    .setMediaSourceFactory(mediaSourceFactory)
    .build()
adsLoader.setPlayer(player)

val adsItem = MediaItem.Builder()
    .setUri("https://example.com/content.m3u8")
    .setAdsConfiguration(
        MediaItem.AdsConfiguration.Builder(android.net.Uri.parse("https://example.com/vast.xml")).build()
    )
    .build()

player.setMediaItem(adsItem)
player.prepare()
```

Release order matters:

```kotlin
override fun onDestroy() {
    player.release()
    adsLoader.release()
    super.onDestroy()
}
```

### WRONG

```kotlin
// WRONG: managing ad playback in a separate overlay Player defeats Media3's ad-scheduling integration
val adPlayer = ExoPlayer.Builder(context).build()
val contentPlayer = ExoPlayer.Builder(context).build()
```

## Step 4: provide the AdViewProvider in Compose

When using `PlayerSurface` from `media3-ui-compose`, supply a `ViewGroup` for ad overlays.

```kotlin
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.FrameLayout
import androidx.media3.common.AdViewProvider

@Composable
fun AdViewAnchor(onAdViewGroup: (FrameLayout) -> Unit) {
    AndroidView(
        factory = { ctx -> FrameLayout(ctx).also(onAdViewGroup) },
        modifier = Modifier.matchParentSize(),
    )
}
```

Pass the `FrameLayout` into an `AdViewProvider` implementation and hand that provider to `DefaultMediaSourceFactory.setLocalAdInsertionComponents`.

**DO NOT** use a `Box` with a transparent background instead of a real `ViewGroup`. IMA attaches ad views as Android Views, not composables.

## Step 5: SSAI / DAI with ImaServerSideAdInsertionMediaSource

### RIGHT

```kotlin
import androidx.media3.exoplayer.ima.ImaServerSideAdInsertionMediaSource
import androidx.media3.exoplayer.ima.ImaServerSideAdInsertionUriBuilder

val ssaiUri = ImaServerSideAdInsertionUriBuilder()
    .setAssetKey("liveAssetKey")
    .setFormat(androidx.media3.common.C.CONTENT_TYPE_HLS)
    .build()

val ssaiLoader = ImaServerSideAdInsertionMediaSource.AdsLoader.Builder(context, playerView /* AdViewProvider */)
    .build()

val ssaiFactory = ImaServerSideAdInsertionMediaSource.Factory(ssaiLoader, mediaSourceFactory)

val ssaiPlayer = ExoPlayer.Builder(context)
    .setMediaSourceFactory(ssaiFactory)
    .build()
ssaiLoader.setPlayer(ssaiPlayer)

ssaiPlayer.setMediaItem(MediaItem.fromUri(ssaiUri))
ssaiPlayer.prepare()
```

**DO NOT** mix SSAI and CSAI in the same playlist. The 1.9.0 IMA extension does not support a second IMA server-side stream in the same session.

## Step 6: companion ads

```kotlin
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot

val companionSlot = ImaSdkFactory.getInstance().createCompanionAdSlot().apply {
    container = companionViewGroup
    setSize(300, 250)
}
val ssaiLoader = ImaServerSideAdInsertionMediaSource.AdsLoader.Builder(context, playerView)
    .setCompanionAdSlots(listOf(companionSlot))
    .build()
```

## Step 7: error handling for ads vs content

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_REMOTE_ERROR -> reportAdServerError(error)
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> reportNetworkError(error)
            else -> reportContentError(error)
        }
    }
})
```

**DO NOT** retry ad playback on every error. IMA already retries failed VAST fetches internally.

## Step 8: analytics correlation

IMA emits its own event stream (impression, quartile, click, complete). Media3 emits `AnalyticsListener` events. Correlate by:

- Session ID (app-generated, passed to both).
- Ad position (`Player.currentAdIndexInAdGroup`, `Player.currentAdGroupIndex`).

Store both streams in the same session bucket so product can see ad-induced abandonment.

## Step 9: release and lifecycle

```kotlin
override fun onStart() {
    super.onStart()
    adsLoader.setPlayer(player)
}

override fun onStop() {
    super.onStop()
    adsLoader.setPlayer(null)
}

override fun onDestroy() {
    player.release()
    adsLoader.release()
    super.onDestroy()
}
```

## Common pitfalls

- **Separate overlay Player for ads.** Media3 integrates ads in the main timeline. Two players thrash decoders.
- **Missing `AdViewProvider`.** IMA cannot render ad overlays.
- **Missing release of `ImaAdsLoader`.** Leaks across activity recreation.
- **Mixing CSAI and SSAI in the same playlist.** Unsupported in 1.9.0.
- **Retrying ad playback on every error.** IMA already retries.
- **Not correlating ad and content analytics.** Product team loses visibility into ad-induced drop-off.
- **Companion slots added after launch.** Hard to retrofit, ad ops usually negotiates at deal time.
- **Ad tag URL filtered by server.** Always verify with the app's production User-Agent.
