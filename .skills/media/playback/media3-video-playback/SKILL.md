---
name: media3-video-playback
description: Use this skill to ship video playback with AndroidX Media3 1.10.0. Use this skill to configure a PlayerSurface with the correct resize mode, negotiate HDR tracks with setAllowedVideoJoiningTimeMs, detect HDR capability on the device, enter and exit Picture-in-Picture, handle display cutouts and notches, and react to surface-level events (onRenderedFirstFrame, onVideoSizeChanged, onSurfaceSizeChanged).
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - video
    - playersurface
    - hdr
    - picture-in-picture
    - resize-mode
    - display-cutout

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** include `media3-ui-compose` for `PlayerSurface`.
- Project **MUST NOT** force `SurfaceView` on devices with transparent overlays.
- Project **MUST NOT** change the surface type on a running player.

## Step 1: plan

1. Categorize each video screen: inline, fullscreen, PiP, fullscreen with subtitles.
2. Pick the surface type per screen.
3. Identify HDR requirements.
4. Confirm the manifest entry declares `android:supportsPictureInPicture="true"`.
5. Identify every place that computes aspect ratio. Use `onVideoSizeChanged` as the single source of truth.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer  = { module = "androidx.media3:media3-exoplayer",  version.ref = "media3" }
media3-ui-compose = { module = "androidx.media3:media3-ui-compose", version.ref = "media3" }
```

## Step 3: Compose PlayerSurface with resize mode

### RIGHT

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW

@Composable
fun FullscreenVideo(player: Player) {
    var aspect by remember { mutableStateOf(16f / 9f) }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(size: androidx.media3.common.VideoSize) {
                if (size.width > 0 && size.height > 0) {
                    aspect = (size.width * size.pixelWidthHeightRatio) / size.height
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    Box(Modifier.fillMaxWidth().aspectRatio(aspect)) {
        PlayerSurface(player = player, surfaceType = SURFACE_TYPE_SURFACE_VIEW)
    }
}
```

### WRONG

```kotlin
// WRONG: hard-coding aspect ratio stretches or letterboxes every non-16:9 source
Box(Modifier.aspectRatio(16f / 9f)) { PlayerSurface(player = player) }
```

## Step 4: detect HDR capability

```kotlin
import android.view.Display.HdrCapabilities

fun supportsHdr10(activity: android.app.Activity): Boolean {
    val hdr = activity.display?.hdrCapabilities ?: return false
    return hdr.supportedHdrTypes.any { it == HdrCapabilities.HDR_TYPE_HDR10 }
}
```

```kotlin
import androidx.media3.common.MimeTypes

if (!supportsHdr10(activity)) {
    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
        .setPreferredVideoMimeType(MimeTypes.VIDEO_H264)
        .build()
}
```

## Step 5: Picture-in-Picture

### Manifest

```xml
<activity
    android:name=".PlayerActivity"
    android:supportsPictureInPicture="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"
    android:launchMode="singleTask" />
```

### Enter PiP

```kotlin
import android.app.PictureInPictureParams
import android.util.Rational

fun enterPip(activity: android.app.Activity, player: Player) {
    val size = player.videoSize
    val aspect = if (size.width > 0 && size.height > 0)
        Rational(size.width, size.height) else Rational(16, 9)

    val params = PictureInPictureParams.Builder()
        .setAspectRatio(aspect)
        .setAutoEnterEnabled(true)
        .build()
    activity.enterPictureInPictureMode(params)
}
```

## Step 6: handle display cutouts and notches

```kotlin
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding

Box(Modifier.padding(WindowInsets.displayCutout.asPaddingValues())) {
    // Overlay controls only. The surface itself should extend edge-to-edge.
}
```

## Step 7: first-frame rendered as a loading signal

```kotlin
import androidx.media3.exoplayer.analytics.AnalyticsListener

val listener = object : AnalyticsListener {
    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        loadingIndicator.hide()
    }
}
player.addAnalyticsListener(listener)
```

## Step 8: surface size changes

```kotlin
override fun onSurfaceSizeChanged(eventTime: AnalyticsListener.EventTime, width: Int, height: Int) {
    val maxBitrate = if (height < 360) 600_000 else 6_000_000
    player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
        .setMaxVideoBitrate(maxBitrate)
        .build()
}
```

## Step 9: subtitles

Subtitle rendering is automatic when the Compose `PlayerControls` from `media3-ui-compose-material3` is in the composition.

## Common pitfalls

- **Hard-coded aspect ratio.**
- **Forcing HDR on SDR panels.**
- **Missing `supportsPictureInPicture` in manifest.**
- **Entering PiP while paused on Android 15.**
- **Padding the surface around the cutout.**
- **Hiding loading on `STATE_READY` instead of `onRenderedFirstFrame`.**
- **Using `SurfaceView` in a scrolling list.**
- **Changing surface type on a running player.**
