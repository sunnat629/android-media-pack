---
name: media3-compose-ui-material3
description: Use this skill to build a Jetpack Compose UI for Media3 playback using the media3-ui-compose-material3 building blocks introduced in 1.10.0. Use this skill to compose ContentFrame, PlayPauseButton, SeekBackButton, and SeekForwardButton over a Player obtained from a MediaController bound to a MediaSessionService, scope state updates correctly to the composition, and keep UnstableApi opt-ins at the narrowest site rather than as a global compiler flag.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - compose
    - material3
    - contentframe
    - playpausebutton
    - seekbackbutton
    - seekforwardbutton
    - mediacontroller
    - unstableapi

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** use Compose BOM 2025.11 or later with Material3 1.4 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST NOT** wrap a legacy `PlayerView` in `AndroidView` in the RIGHT path.
- Project **MUST NOT** enable a global `-opt-in=androidx.media3.common.util.UnstableApi` compiler flag.
- A `MediaSessionService` **MUST** be running to host the `Player`.

## Step 1: plan

1. Confirm the app already has a `MediaSessionService`.
2. Enumerate every place that constructs an `ExoPlayer` from Compose code.
3. Grep for `AndroidView` wrapping `PlayerView`.
4. Grep for a global `-opt-in=androidx.media3.common.util.UnstableApi`.
5. Confirm the theme applies Material3.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"
compose-bom = "2025.11.00"
material3 = "1.4.0"

[libraries]
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-material3 = { module = "androidx.compose.material3:material3", version.ref = "material3" }

media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-session   = { module = "androidx.media3:media3-session",   version.ref = "media3" }
media3-ui-compose-material3 = { module = "androidx.media3:media3-ui-compose-material3", version.ref = "media3" }
```

## Step 3: obtain the Player via a MediaController

### RIGHT

```kotlin
import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun rememberMediaController(): Player? {
    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }
    DisposableEffect(context) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({ player = future.get() }, MoreExecutors.directExecutor())
        onDispose {
            (player as? MediaController)?.release()
            player = null
        }
    }
    return player
}
```

### WRONG

```kotlin
// WRONG: constructing ExoPlayer inside a composable leaks the player across recompositions
@Composable
fun BadPlayer() {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
}
```

## Step 4: render a video with ContentFrame

### RIGHT

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.PlayPauseButton
import androidx.media3.ui.compose.material3.SeekBackButton
import androidx.media3.ui.compose.material3.SeekForwardButton

@OptIn(UnstableApi::class)
@Composable
fun PlayerUi(player: Player, modifier: Modifier = Modifier) {
    Column(modifier) {
        ContentFrame(player = player, modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeekBackButton(player = player)
            PlayPauseButton(player = player)
            SeekForwardButton(player = player)
        }
    }
}
```

### WRONG

```kotlin
// WRONG: wrapping legacy PlayerView in AndroidView bypasses media3-ui-compose-material3
@Composable
fun BadPlayerUi(player: Player) {
    AndroidView(factory = { ctx -> PlayerView(ctx).also { it.player = player } })
}
```

## Step 5: observe player state with composable helpers

### RIGHT

```kotlin
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPresentationState

@OptIn(UnstableApi::class)
@Composable
fun PlayerHeader(player: Player) {
    val presentation by rememberPresentationState(player)
    val playPause by rememberPlayPauseButtonState(player)
    Text(
        text = if (playPause.showPlay) "Paused" else "Playing",
        modifier = Modifier.padding(8.dp),
    )
}
```

**DO NOT** attach a raw `Player.Listener` from a composable without wrapping it in `DisposableEffect`. The listener will leak across recompositions.

## Step 6: scope UnstableApi opt-ins narrowly

### RIGHT

```kotlin
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(player: Player) { /* ... */ }
```

### WRONG

```kotlin
// WRONG in build.gradle.kts: a global opt-in hides accidental UnstableApi use
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.media3.common.util.UnstableApi")
    }
}
```

## Step 7: respect lifecycle

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
fun PlayerGate(content: @Composable () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            active = event.targetState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    if (active) content()
}
```

## Step 8: theming and Material3 integration

### RIGHT

```kotlin
@Composable
fun App() {
    ShunnekMaterial3Theme {
        val player = rememberMediaController()
        if (player != null) PlayerScreen(player)
    }
}
```

**PREFERRED** is inheriting button colors from the Material3 theme. Hard-coding colors on `PlayPauseButton` and the seek buttons breaks dynamic color support.

## Common pitfalls

- **Creating `ExoPlayer` inside a composable.** Leaks the player across recompositions and configuration changes. Always go through `MediaController`.
- **Wrapping `PlayerView` in `AndroidView`.** Defeats the purpose of `media3-ui-compose-material3`.
- **Global `UnstableApi` opt-in.** Hides accidental usage of unstable APIs. Keep `@OptIn` at the narrowest scope.
- **Manual `Player.Listener` without `DisposableEffect`.** Leaks listeners across recompositions.
- **Hard-coded button colors.** Breaks dynamic color and theme switching.
- **Forgetting to release the `MediaController`.** Hold the reference for the life of the composition and release in `onDispose`.
- **Using the legacy `PlayerView` PiP path in Compose.** Compose owns its own PiP integration via `ContentFrame`.
- **Blocking the main thread while awaiting the controller.** `buildAsync().addListener(...)` is the correct pattern; `get()` on the main thread will ANR.
