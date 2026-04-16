---
name: media3-compose-ui-material3
description: Use this skill to build a Jetpack Compose UI for Media3 playback using the media3-ui-compose-material3 building blocks introduced in 1.9.0. Use this skill to compose ContentFrame, PlayPauseButton, SeekBackButton, and SeekForwardButton over a Player obtained from a MediaController bound to a MediaSessionService, scope state updates correctly to the composition, and keep UnstableApi opt-ins at the narrowest site rather than as a global compiler flag.
license: Complete terms in LICENSE.txt
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.9.0"
  last_reviewed: "2026-04-16"
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
- Project **MUST** pin Media3 to **1.9.0** or later. Earlier releases ship the Compose UI as experimental.
- Project **MUST NOT** wrap a legacy `PlayerView` in `AndroidView` in the RIGHT path. That pattern belongs to `migrate-exoplayer-to-media3`.
- Project **MUST NOT** enable a global `-opt-in=androidx.media3.common.util.UnstableApi` compiler flag. Scope opt-ins per composable.
- A `MediaSessionService` **MUST** be running to host the `Player`. Apps that only need preview playback may construct an `ExoPlayer` directly, but the RIGHT path for production is a `MediaController` bound to a `MediaSessionService`.

## Step 1: plan

Before composing the UI, do the following:

1. Confirm the app already has a `MediaSessionService`. If not, author that skill first (`media3-background-playback-service`).
2. Enumerate every place that constructs an `ExoPlayer` from Compose code. Flag each for replacement with a `MediaController`.
3. Grep for `AndroidView` wrapping `PlayerView`. Each occurrence is a candidate for replacement with `ContentFrame`.
4. Grep for a global `-opt-in=androidx.media3.common.util.UnstableApi`. Remove it and replace with `@OptIn(UnstableApi::class)` on each composable that uses an unstable API.
5. Confirm the theme applies Material3 (`MaterialTheme` from `androidx.compose.material3`). The Material3 composables in `media3-ui-compose-material3` inherit theme defaults.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"
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

**DO NOT** include `media3-ui` alongside `media3-ui-compose-material3` unless the app still renders a legacy `PlayerView` somewhere.

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
// WRONG: constructing ExoPlayer inside a composable leaks the player across recomposition and disobeys the single-player rule
@Composable
fun PlayerScreen() {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    // ...
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
import androidx.media3.ui.compose.material3.ContentFrame
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

### WRONG

```kotlin
// WRONG: wrapping a legacy PlayerView in AndroidView defeats the purpose of the Compose UI module
@Composable
fun PlayerUi(player: Player) {
    AndroidView(factory = { context -> PlayerView(context).apply { this.player = player } })
}
```

## Step 5: observe player state with composable helpers

Media3 ships composable-friendly state holders. **DO NOT** subscribe to the `Player` with an ad-hoc `Player.Listener` inside a `LaunchedEffect` unless a helper does not already cover the case.

### RIGHT

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

### WRONG

```kotlin
// WRONG: manual Player.Listener without DisposableEffect leaks listeners across recompositions
@Composable
fun PlayerHeader(player: Player) {
    var isPlaying by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(value: Boolean) { isPlaying = value }
        })
    }
}
```

## Step 6: scope UnstableApi opt-ins narrowly

Media3 marks many Compose APIs with `@UnstableApi` while they stabilize. Opt in at the function that calls them, never at the module level.

### RIGHT

```kotlin
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(player: Player) { /* ... */ }
```

### WRONG

```kotlin
// WRONG: global compiler opt-in hides real surface-area changes when upgrading Media3
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-opt-in=androidx.media3.common.util.UnstableApi")
    }
}
```

## Step 7: respect lifecycle

### RIGHT

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

**DO NOT** leave a video decoding in a backgrounded composable. Wrap heavyweight player UI in a lifecycle-aware gate to save battery.

## Step 8: theming and Material3 integration

`ContentFrame`, `PlayPauseButton`, `SeekBackButton`, and `SeekForwardButton` read from `MaterialTheme`. The **PREFERRED** approach is to provide a coherent Material3 theme and let the buttons inherit tonal colors, typography, and corner radii.

```kotlin
@Composable
fun App() {
    ShunnekMaterial3Theme {
        val player = rememberMediaController()
        if (player != null) PlayerScreen(player)
    }
}
```

**DO NOT** recolor the Media3 buttons with hand-coded `Color` values. Change the Material3 scheme instead.

## Common pitfalls

- **Creating `ExoPlayer` inside a composable.** Leaks on recomposition. Use a `MediaController` bound to a `MediaSessionService`.
- **Wrapping `PlayerView` in `AndroidView`.** Defeats the purpose of the Compose UI module. Use `ContentFrame` instead.
- **Global `UnstableApi` opt-in.** Masks real surface-area changes on upgrade. Use `@OptIn` per composable.
- **Manual `Player.Listener` without `DisposableEffect`.** Leaks listeners. Use the `remember...State` helpers from `androidx.media3.ui.compose.state`.
- **Hard-coded button colors.** Break in dark mode and on dynamic color devices. Theme at the Material3 level.
- **Forgetting to release the `MediaController`.** Ties up the session. Always release in `onDispose`.
- **Using the legacy PlayerView PiP path in Compose.** PiP plumbing now belongs in the activity, not in the composable.
- **Blocking the main thread while awaiting the controller.** The controller future must be observed asynchronously.

## Checklist

- [ ] `minSdk` 21 or later, Media3 pinned to 1.9.0 or later, Compose BOM 2025.11+, Material3 1.4+.
- [ ] `MediaController` obtained via `rememberMediaController()` or equivalent `DisposableEffect`, released on dispose.
- [ ] UI renders `ContentFrame`, `PlayPauseButton`, `SeekBackButton`, `SeekForwardButton` from `androidx.media3.ui.compose.material3`.
- [ ] No `AndroidView` wrapping of `PlayerView` in the RIGHT path.
- [ ] No global `-opt-in=androidx.media3.common.util.UnstableApi` compiler flag. All opt-ins are per composable.
- [ ] Player state observed via `remember...State` helpers from `androidx.media3.ui.compose.state`, not ad-hoc listeners.
- [ ] Player UI gated by a lifecycle observer so decoding stops when backgrounded.
- [ ] Theming inherits from `MaterialTheme`. No hand-coded button colors.
- [ ] Device matrix (Pixel 6 API 33, Pixel 8 API 34, Pixel Tablet, Pixel Fold API 35) passes all scenarios from the validation plan.
