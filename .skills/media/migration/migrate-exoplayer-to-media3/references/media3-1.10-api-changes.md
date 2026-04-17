# Media3 1.10.0 API changes relevant to migration

These are the 1.10.0-specific API shifts referenced from the main SKILL.md. Verify each claim against the Media3 1.10.0 release notes and the `androidx/media` source before shipping.

## setMediaButtonPreferences replaces setCustomLayout

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

## player.mute() and player.unmute()

The `Player` interface gains `mute()` and `unmute()` helpers. Previous code cached `player.volume` before muting and restored it on unmute. Replace that pattern.

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

## StuckPlayerException

`StuckPlayerException` surfaces through `Player.Listener.onPlayerError` when the player makes no progress for a configurable window. Log it as a distinct error class in analytics so stuck sessions do not hide inside generic `PlaybackException` buckets.

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

## Default-on wake lock

Media3 1.10.0 acquires and releases the playback wake lock internally. **DO NOT** wrap playback in `PowerManager.WakeLock`. Remove any legacy `setWakeMode`-adjacent scaffolding that duplicates this behavior.

## CastPlayer.setLocalPlayer

`CastPlayer.Builder(context).setLocalPlayer(exoPlayer).build()` replaces the prior pattern where the app maintained two `Player` instances and swapped them when a Cast route was selected. One `Player` reference is exposed to the `MediaSession`; Cast switching is internal.
