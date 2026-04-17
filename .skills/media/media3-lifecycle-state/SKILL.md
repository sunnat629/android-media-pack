---
name: media3-lifecycle-state
description: Use this skill to wire lifecycle-aware playback in an AndroidX Media3 1.9.0 app. Use this skill to obtain a MediaController via DisposableEffect, gate video decoding by activity lifecycle, persist playback position via SavedStateHandle, recover cleanly from configuration changes and system-initiated process death, and release the controller exactly once per host.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - lifecycle
    - mediacontroller
    - process-death
    - savedstatehandle
    - configuration-change
    - disposable-effect

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.9.0** or later.
- Project **MUST** run playback inside a `MediaSessionService` (see the `media3-background-playback-service` skill). Lifecycle-aware wiring in this skill assumes the service is the owner of the `ExoPlayer`.
- Project **MUST NOT** hold a static reference to the `ExoPlayer` from an activity, fragment, or composable.
- Project **MUST NOT** release the `MediaController` on a screen rotation. Configuration changes are not process death.

## Step 1: plan

1. Grep for `ExoPlayer.Builder(` inside any activity, fragment, or composable. Each hit is a bug: the player belongs inside the `MediaSessionService`.
2. Enumerate every navigation exit point (back button, system back, deep link, picture-in-picture). Each exit **MUST** call `MediaController.release()` exactly once.
3. Confirm the app saves only the media item ID and the playback position in `SavedStateHandle`. **DO NOT** save a `Player` reference.
4. Identify every screen that shows video. Each one **MUST** be gated by a lifecycle observer so decoding pauses when backgrounded.
5. Confirm the app does not rely on `onStop` to release the service. The session owns the foreground lifecycle independent of the UI.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"

[libraries]
media3-session = { module = "androidx.media3:media3-session", version.ref = "media3" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version = "2.8.7" }
```

## Step 3: obtain a MediaController in Compose

### RIGHT

```kotlin
import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun rememberPlayer(): Player? {
    val context = LocalContext.current
    val playerState = remember { mutableStateOf<Player?>(null) }
    DisposableEffect(context) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({ playerState.value = future.get() }, MoreExecutors.directExecutor())
        onDispose {
            (playerState.value as? MediaController)?.release()
            playerState.value = null
        }
    }
    return playerState.value
}
```

### WRONG

```kotlin
// WRONG: releasing on recomposition kills the controller every state change
@Composable
fun PlayerScreen() {
    val controller = MediaController.Builder(context, token).buildAsync().get()
    // forgotten release, leaks
}
```

## Step 4: gate decoding by lifecycle

Video decoders are heavy. Stop rendering when the screen is not visible.

### RIGHT

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun VideoGate(content: @Composable () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumed by remember { mutableStateOf(lifecycle.currentState == Lifecycle.State.RESUMED) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            resumed = event.targetState == Lifecycle.State.RESUMED
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    if (resumed) content()
}
```

**DO NOT** keep a backgrounded video surface rendering. The battery cost is real on mid-range devices.

## Step 5: persist resume state across process death

### RIGHT

```kotlin
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PlaybackViewModel(private val state: SavedStateHandle) : ViewModel() {

    fun saveResume(mediaId: String, positionMs: Long) {
        state["resume.id"] = mediaId
        state["resume.pos"] = positionMs
    }

    val resumeMediaId: String? get() = state["resume.id"]
    val resumePositionMs: Long get() = state["resume.pos"] ?: 0L
}
```

On resume, seek the controller to the saved position only once.

```kotlin
LaunchedEffect(player, vm.resumeMediaId) {
    val id = vm.resumeMediaId ?: return@LaunchedEffect
    if (player.currentMediaItem?.mediaId != id) return@LaunchedEffect
    player.seekTo(vm.resumePositionMs)
}
```

### WRONG

```kotlin
// WRONG: stashing the Player in a static field survives config change but leaks on process death
object PlayerHolder { var player: Player? = null }
```

## Step 6: handle configuration changes

Configuration changes (rotation, dark mode, locale) recreate the activity but the `MediaSessionService` stays alive. The `MediaController` in the recreated activity **MUST** reconnect via `SessionToken`.

**DO NOT** set `android:configChanges="orientation|screenSize"` on the player activity as a workaround. Reconnecting is cheap and compatible with Compose state handoff.

## Step 7: release exactly once per host

A Compose host releases on `onDispose`. An XML activity releases in `onDestroy`. **DO NOT** release in both `onPause` and `onStop`.

```kotlin
class PlayerActivity : AppCompatActivity() {
    private var controller: MediaController? = null

    override fun onStart() {
        super.onStart()
        // Build controller
    }

    override fun onStop() {
        super.onStop()
        controller?.release()
        controller = null
    }
}
```

## Step 8: survive PiP transitions

Picture-in-Picture keeps the activity alive but calls `onStop` on the primary window. Detect PiP mode before releasing the controller.

```kotlin
override fun onStop() {
    super.onStop()
    if (!isInPictureInPictureMode) {
        controller?.release()
        controller = null
    }
}
```

## Step 9: service lifecycle stays outside the UI

The `MediaSessionService` owns its own foreground lifecycle. UI activities and composables **MUST NOT** call `stopService(intent)` on the playback service. Let `onTaskRemoved` inside the service decide when to stop.

## Common pitfalls

- **Static `ExoPlayer` or `Player` reference.** Leaks on process death, breaks Auto and Wear.
- **Releasing on every recomposition.** Thrashes the session.
- **Video surface rendering while backgrounded.** Battery drain.
- **Saving a `Player` in `SavedStateHandle`.** Not parcelable. Save IDs and positions only.
- **Setting `android:configChanges` on the player activity.** Hides bugs rather than fixing them.
- **Releasing the controller in both `onPause` and `onStop`.** Double release throws.
- **Calling `stopService` on `MediaSessionService` from the UI.** The service owns its lifecycle.
- **Ignoring PiP in `onStop`.** Kills the controller the moment the user enters PiP.
