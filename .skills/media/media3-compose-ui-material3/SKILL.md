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

## Step 4: render a video with ContentFrame

### RIGHT

```kotlin
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeekBackButton(player = player)
            PlayPauseButton(player = player)
            SeekForwardButton(player = player)
        }
    }
}
```

## Step 5: observe player state with composable helpers

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.media3.common.Player
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

## Step 6: scope UnstableApi opt-ins narrowly

```kotlin
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(player: Player) { /* ... */ }
```

## Step 7: respect lifecycle

```kotlin
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.DisposableEffect

@Composable
fun PlayerGate(content: @Composable () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            active = event.targetState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    if (active) content()
}
```

## Step 8: theming and Material3 integration

```kotlin
@Composable
fun App() {
    ShunnekMaterial3Theme {
        val player = rememberMediaController()
        if (player != null) PlayerScreen(player)
    }
}
```

## Common pitfalls

- **Creating `ExoPlayer` inside a composable.**
- **Wrapping `PlayerView` in `AndroidView`.**
- **Global `UnstableApi` opt-in.**
- **Manual `Player.Listener` without `DisposableEffect`.**
- **Hard-coded button colors.**
- **Forgetting to release the `MediaController`.**
- **Using the legacy PlayerView PiP path in Compose.**
- **Blocking the main thread while awaiting the controller.**
