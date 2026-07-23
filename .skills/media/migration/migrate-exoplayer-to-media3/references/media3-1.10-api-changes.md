# API changes relevant when migrating to Media3 1.10.x

These are API shifts you will encounter when landing on Media3 1.10.x, referenced from the main SKILL.md. Not all of them were introduced in 1.10.0; each section states the version that introduced the change. Verify each claim against the Media3 release notes and the `androidx/media` source before shipping.

## setMediaButtonPreferences replaces setCustomLayout (1.5.0 / 1.6.0)

Introduced in Media3 1.5.0; `setCustomLayout` was deprecated in 1.6.0.

`MediaSession.setCustomLayout` is superseded by `MediaSession.setMediaButtonPreferences`. Buttons are declared with `CommandButton.Builder` and bound to standard `Player.COMMAND_*` constants instead of custom `SessionCommand` ids.

```kotlin
import androidx.media3.common.Player
import androidx.media3.session.CommandButton

session.setMediaButtonPreferences(
    listOf(
        CommandButton.Builder(CommandButton.ICON_FAST_FORWARD)
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .build(),
        CommandButton.Builder(CommandButton.ICON_REWIND)
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .build(),
    )
)
```

**DO NOT** route built-in player actions through a custom `SessionCommand`. Use `setPlayerCommand` with the matching `Player.COMMAND_*`.

## player.mute() and player.unmute() (added 1.9.0, stable in 1.10.0)

The `Player` interface gains `mute()` and `unmute()` helpers, added in Media3 1.9.0 and stable in 1.10.0. Previous code cached `player.volume` before muting and restored it on unmute. Replace that pattern.

```kotlin
// RIGHT
player.mute()
player.unmute()
```

```kotlin
// WRONG
private var savedVolume = 1f
fun mute() { savedVolume = player.volume; player.volume = 0f }
fun unmute() { player.volume = savedVolume }
```

## StuckPlayerException (introduced 1.9.0)

Introduced in Media3 1.9.0. `StuckPlayerException` surfaces through `Player.Listener.onPlayerError` when the player makes no progress for a configurable window. Log it as a distinct error class in analytics so stuck sessions do not hide inside generic `PlaybackException` buckets.

```kotlin
player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        if (error is StuckPlayerException) {
            analytics.logStuck(error)
        }
    }
})
```

## selectTextByDefault on TrackSelectionParameters

`TrackSelectionParameters.Builder` exposes `setSelectTextByDefault(Boolean)`. Prefer this over manually toggling `preferredTextLanguages` to force subtitles on at start.

```kotlin
player.trackSelectionParameters = player.trackSelectionParameters
    .buildUpon()
    .setSelectTextByDefault(true)
    .build()
```

## Default-on wake lock (since 1.9.0)

Since Media3 1.9.0 the player acquires and releases the playback wake lock internally by default. **DO NOT** wrap playback in `PowerManager.WakeLock`. Remove any legacy `setWakeMode`-adjacent scaffolding that duplicates this behavior.

## CastPlayer.setLocalPlayer (Builder rewrite in 1.9.0)

The `CastPlayer` Builder rewrite landed in Media3 1.9.0. `CastPlayer.Builder(context).setLocalPlayer(exoPlayer).build()` replaces the prior pattern where the app maintained two `Player` instances and swapped them when a Cast route was selected. One `Player` reference is exposed to the `MediaSession`; Cast switching is internal.
