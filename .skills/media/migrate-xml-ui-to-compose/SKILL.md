---
name: migrate-xml-ui-to-compose
description: Use this skill to migrate an Android media UI from XML-based PlayerView (media3-ui) to Jetpack Compose (media3-ui-compose and media3-ui-compose-material3) for Media3 1.9.0. Use this skill to translate PlayerControlView behavior into PlayerSurface and PlayerControls composables, map controller_layout_id overrides to Compose slots, preserve fullscreen and subtitle toggles, and remove dangling AndroidView interop once the migration is complete.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.9.0"
  last_reviewed: "2026-04-16"
  keywords:
    - android
    - media3
    - compose
    - playerview
    - playersurface
    - ui-compose-material3
    - migration
    - fullscreen

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.9.0** or later.
- Project **MUST** use Compose BOM `2025.11.00` or later, with Material3 `1.4.0` or later.
- Project **MUST NOT** keep both `PlayerView` (XML) and `PlayerSurface` (Compose) attached to the same `Player` instance at the same time. Subtitles and overlays will double-render.
- Project **MUST NOT** write custom `SurfaceView` subclasses. Use the `PlayerSurface` composable.

## Step 1: plan

1. Enumerate every `PlayerView` inclusion in layout XML, plus every place the app inflates `exo_player_control_view` or its custom `controller_layout_id`.
2. Categorize each inclusion: full-screen player, inline player, mini player, PiP host.
3. Identify every custom player control (overflow menu, chapters, settings, quality selector). Each control translates to a Compose slot.
4. Confirm the player instance is a `MediaController` obtained from a `MediaSessionService`. Compose composition and XML inflation **MUST NOT** both own the lifecycle.
5. Plan the rollout: migrate one screen at a time. Keep the old XML screen behind a feature flag until the Compose screen ships to 5% and holds.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"

[libraries]
media3-ui-compose           = { module = "androidx.media3:media3-ui-compose",           version.ref = "media3" }
media3-ui-compose-material3 = { module = "androidx.media3:media3-ui-compose-material3", version.ref = "media3" }
```

## Step 3: translate PlayerView into PlayerSurface

### Before (XML)

```xml
<androidx.media3.ui.PlayerView
    android:id="@+id/player_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:resize_mode="fit"
    app:use_controller="true"
    app:show_subtitle_button="true"
    app:show_fullscreen_button="true" />
```

### After (Compose, RIGHT)

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.material3.PlayerControls

@Composable
fun ContentPlayer(player: Player, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
        PlayerSurface(player = player, surfaceType = SURFACE_TYPE_SURFACE_VIEW)
        PlayerControls(player = player)
    }
}
```

### WRONG

```kotlin
// WRONG: keeping PlayerView via AndroidView while also using PlayerSurface double-renders subtitles
AndroidView(factory = { PlayerView(it) }, update = { it.player = player })
PlayerSurface(player = player)
```

## Step 4: map custom controller_layout_id

The XML `controller_layout_id` replaced individual buttons. In Compose, you slot composables into the provided controls region or build your own.

```kotlin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Settings

@Composable
fun CustomControls(player: Player, onOpenChapters: () -> Unit, onOpenSettings: () -> Unit) {
    PlayerControls(
        player = player,
        overflow = {
            IconButton(onClick = onOpenChapters) {
                Icon(Icons.Default.ClosedCaption, contentDescription = "Chapters")
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
    )
}
```

## Step 5: preserve fullscreen

### RIGHT

```kotlin
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun FullscreenPlayerHost(player: Player) {
    var fullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(fullscreen) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        val insets = activity.window.insetsController ?: return@LaunchedEffect
        if (fullscreen) {
            insets.hide(WindowInsets.Type.systemBars())
            insets.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insets.show(WindowInsets.Type.systemBars())
        }
    }

    BackHandler(enabled = fullscreen) { fullscreen = false }

    if (fullscreen) FullscreenContent(player, onExit = { fullscreen = false })
    else InlineContent(player, onEnter = { fullscreen = true })
}
```

### WRONG

```kotlin
// WRONG: calling setSystemUiVisibility from an XML PlayerView's fullscreen listener while also
// using Compose windowInsets controller fights itself. Pick one source of truth.
```

## Step 6: subtitles

Compose `PlayerControls` from `media3-ui-compose-material3` renders the subtitle toggle and selection sheet when the current item has text tracks. No extra wiring is needed.

**DO NOT** implement a custom subtitle rail when the built-in controls already expose it. User research consistently shows users look for the CC icon in the bottom-right.

## Step 7: remove AndroidView interop once the migration is complete

When all screens use Compose, delete:

- `exo_player_control_view.xml` overrides.
- `app:controller_layout_id` attributes.
- `androidx.compose.ui.viewinterop.AndroidView` wrappers around `PlayerView`.

**DO NOT** keep one XML screen "just in case". It prevents removing the `media3-ui` dependency.

## Step 8: dependency cleanup

After the final XML screen is gone:

```toml
# Remove from libs.versions.toml and dependencies blocks:
# media3-ui = { module = "androidx.media3:media3-ui", version.ref = "media3" }
```

Verify with `./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep media3-ui`. The line **MUST NOT** appear.

## Step 9: rollout plan

1. Ship the Compose inline player first. It has the smallest regression surface.
2. Ship the Compose fullscreen screen next.
3. Ship PiP and Cast last. These have system UI entanglements.
4. Keep XML screens behind a feature flag until each stage holds for a full release cycle.

## Step 10: verify PiP still works

PiP declares the Activity, not the Composable. The manifest entry stays the same:

```xml
<activity
    android:name=".PlayerActivity"
    android:supportsPictureInPicture="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" />
```

The Compose tree **MUST** react to PiP entry by hiding the overlay controls and shrinking the surface.

## Common pitfalls

- **Both `PlayerView` and `PlayerSurface` attached to the same `Player`.** Double rendering.
- **Custom `SurfaceView` subclass.** `PlayerSurface` already handles surface lifecycle.
- **Manual system-UI fullscreen fights Compose `WindowInsetsController`.** Pick one source of truth.
- **Leaving `controller_layout_id` XML after migration.** Dead code confuses later readers.
- **Dropping `media3-ui` dependency while an XML screen still uses it.** Build breaks.
- **Missing `supportsPictureInPicture` in the manifest.** PiP entry no-ops silently.
- **Releasing the `MediaController` on a fullscreen toggle.** Toggle is not process death.
- **Keeping the XML screen behind a flag forever.** Delete it once the Compose screen holds.
