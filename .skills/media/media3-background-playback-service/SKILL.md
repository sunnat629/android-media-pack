---
name: media3-background-playback-service
description: Use this skill to build a production-grade Android background playback service using AndroidX Media3 1.9.0. Use this skill to subclass MediaSessionService, construct an ExoPlayer wrapped by a MediaSession, declare the service with foregroundServiceType="mediaPlayback", hold the FOREGROUND_SERVICE_MEDIA_PLAYBACK permission, connect external controllers with MediaController.Builder, let the service generate the playback notification, honor Android 14+ user-initiated foreground service rules, and stop cleanly on onTaskRemoved.
license: Complete terms in LICENSE.txt
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.9.0"
  last_reviewed: "2026-04-16"
  keywords:
    - android
    - media3
    - mediasessionservice
    - mediasession
    - mediacontroller
    - background-playback
    - foreground-service
    - android14
    - exoplayer
---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** use AGP 8.0 or later and Kotlin 1.9 or later.
- Project **MUST** pin Media3 to **1.9.0** or later. Earlier releases lack `mute()`, automatic wake lock, `setMediaButtonPreferences`, and the stuck-player detection referenced below.
- Project **MUST NOT** depend on both `androidx.media3.*` and legacy `com.google.android.exoplayer2.*` at the same time.
- Project **MUST NOT** use `MediaSessionCompat` or `MediaSessionConnector`. Both are removed in Media3.
- Project **MUST NOT** use `PlayerNotificationManager`. The `MediaSessionService` generates the notification in Media3.
- App **MUST** declare the `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permissions in the manifest, and **MUST** request `POST_NOTIFICATIONS` at runtime on API 33+.

## Step 1: plan

Before writing any service code, do the following:

1. Grep for `MediaSessionCompat`, `MediaSessionConnector`, `PlayerNotificationManager`, and `Service` subclasses that host playback. Flag each for removal or refactor.
2. Enumerate every place the app constructs an `ExoPlayer`. For background playback, exactly one `ExoPlayer` instance **MUST** live inside the `MediaSessionService`. UI code **MUST** talk to it through a `MediaController`, never by holding a direct reference.
3. Flag any `WakeLock` acquisition around playback. Media3 1.9.0 holds wake locks automatically. Manual acquisition is incorrect.
4. Flag any `startForegroundService` calls for playback. The service **MUST** be started by `MediaController.connect` or by a user-visible action. Background code paths **MUST NOT** promote the service to the foreground on Android 14+.
5. Confirm the manifest does not declare `android:stopWithTask="true"`. The service **MUST** handle task removal explicitly in `onTaskRemoved`.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.9.0"

[libraries]
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-session   = { module = "androidx.media3:media3-session",   version.ref = "media3" }
media3-ui-compose-material3 = { module = "androidx.media3:media3-ui-compose-material3", version.ref = "media3" }
```

**DO NOT** add `media3-ui` in addition to `media3-ui-compose-material3` unless the app still renders a legacy `PlayerView` somewhere.

## Step 3: declare permissions and the service in the manifest

### RIGHT

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application>
        <service
            android:name=".playback.PlaybackService"
            android:exported="true"
            android:foregroundServiceType="mediaPlayback">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

### WRONG

```xml
<!-- WRONG: missing foregroundServiceType on Android 14+ triggers a SecurityException -->
<service
    android:name=".playback.PlaybackService"
    android:exported="true" />
```

```xml
<!-- WRONG: missing the MediaSessionService action means MediaController.connect never resolves -->
<service android:name=".playback.PlaybackService" android:foregroundServiceType="mediaPlayback" />
```

## Step 4: subclass MediaSessionService

### RIGHT

```kotlin
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(buildSessionActivityIntent())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val current = player
        if (!current.playWhenReady || current.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun buildSessionActivityIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            /* requestCode = */ 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
```

### WRONG

```kotlin
// WRONG: subclassing Service instead of MediaSessionService loses automatic notification handling
class PlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
```

```kotlin
// WRONG: holding a static reference to the player lets UI bypass the MediaController boundary
object PlayerHolder { lateinit var player: ExoPlayer }
```

## Step 5: connect a MediaController from the UI

The `Activity` or Compose entry point **MUST** use `MediaController.Builder` to talk to the service. **DO NOT** bind to the service directly with `bindService`.

### RIGHT

```kotlin
import android.content.ComponentName
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

class PlaybackConnection(private val context: Context) {

    private var controller: MediaController? = null

    fun connect(onReady: (Player) -> Unit) {
        val token = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java),
        )
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener(
            {
                controller = future.get()
                controller?.let(onReady)
            },
            MoreExecutors.directExecutor(),
        )
    }

    fun release() {
        controller?.release()
        controller = null
    }
}
```

### WRONG

```kotlin
// WRONG: binding manually and casting to PlaybackService leaks the player and breaks on process death
val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val playback = (service as PlaybackService.LocalBinder).service
        playback.player.play()
    }
    override fun onServiceDisconnected(name: ComponentName) {}
}
```

## Step 6: request POST_NOTIFICATIONS at runtime on API 33+

### RIGHT

```kotlin
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* result handling */ }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
```

**DO NOT** call `startForegroundService` from the UI to promote the playback service. The first `MediaController` command that requires playback triggers the foreground transition inside `MediaSessionService`.

## Step 7: customize the notification drawer with setMediaButtonPreferences

In Media3 1.9.0, the notification transport drawer is configured declaratively. **DO NOT** use `setCustomLayout` for commands that map to built-in `Player.COMMAND_*` values.

### RIGHT

```kotlin
import androidx.media3.common.Player
import androidx.media3.session.CommandButton

private fun applyMediaButtonPreferences(session: MediaSession) {
    session.setMediaButtonPreferences(
        listOf(
            CommandButton.Builder(CommandButton.ICON_REWIND)
                .setDisplayName(getString(R.string.skip_back))
                .setPlayerCommand(Player.COMMAND_SEEK_BACK)
                .build(),
            CommandButton.Builder(CommandButton.ICON_FAST_FORWARD)
                .setDisplayName(getString(R.string.skip_forward))
                .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
                .build(),
        )
    )
}
```

### WRONG

```kotlin
// WRONG: reinventing seek with a custom SessionCommand bypasses Player.COMMAND_* wiring and breaks Auto, Wear, and Bluetooth
session.setCustomLayout(
    listOf(CommandButton.Builder().setSessionCommand(SessionCommand("seek_forward", Bundle.EMPTY)).build())
)
```

## Step 8: handle playback errors with StuckPlayerException

Media3 1.9.0 dispatches `StuckPlayerException` when the player stalls without progress. Surface it in analytics and show a recoverable UI state.

### RIGHT

```kotlin
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.StuckPlayerException

player.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        when (error) {
            is StuckPlayerException -> analytics.logStuckPlayer(error)
            else -> analytics.logPlayerError(error)
        }
        player.prepare()
    }
})
```

**DO NOT** catch `StuckPlayerException` and silently retry forever. Cap retries and surface a user-visible error if the stall recurs.

## Step 9: stop cleanly on task removal

The default `MediaSessionService` behavior on task removal is to keep the service alive so playback continues. Override `onTaskRemoved` to match the UX the app wants.

### RIGHT: stop when the user swipes away the activity and playback is paused

```kotlin
override fun onTaskRemoved(rootIntent: Intent?) {
    if (!player.playWhenReady || player.mediaItemCount == 0) {
        stopSelf()
    }
}
```

### WRONG

```kotlin
// WRONG: unconditionally stopping kills background playback even while audio is still playing
override fun onTaskRemoved(rootIntent: Intent?) {
    stopSelf()
}
```

## Common pitfalls

- **Missing `foregroundServiceType`.** On Android 14+ the service cannot promote to the foreground without `android:foregroundServiceType="mediaPlayback"` in the manifest.
- **Missing runtime `POST_NOTIFICATIONS` request.** On API 33+ the notification that `MediaSessionService` posts is silently suppressed if the permission was never granted.
- **Manual wake lock around playback.** Media3 1.9.0 holds the wake lock itself. A second manual wake lock prevents the device from ever sleeping even after playback ends.
- **Holding a static reference to the player.** Bypasses `MediaController`, leaks on process death, and breaks Android Auto and Wear OS.
- **Unconditional `stopSelf` in `onTaskRemoved`.** Breaks the primary background playback user experience.
- **Multiple `ExoPlayer` instances.** Only one player **MUST** exist inside the service. Do not construct another player in the activity.
- **Using `MediaSessionCompat`.** Removed in Media3. Any lingering usage blocks `MediaController` discovery.
- **Swallowing `StuckPlayerException`.** The error exists precisely to surface hard stalls. Log it and surface a recoverable UI state.
