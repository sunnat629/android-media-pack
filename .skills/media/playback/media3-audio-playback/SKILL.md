---
name: media3-audio-playback
description: Use this skill to ship audio-only playback with AndroidX Media3 1.10.0. Use this skill to configure AudioAttributes for music or spoken content, enable handleAudioBecomingNoisy and handleAudioFocus, integrate with a MediaSessionService for lock screen and notification controls, expose chapters and metadata on MediaItem.MediaMetadata, and honor system volume and accessibility focus behavior.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - audio
    - audioattributes
    - audio-focus
    - becoming-noisy
    - mediametadata
    - mediasession

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** run playback from a `MediaSessionService`.
- Project **MUST NOT** set `AudioAttributes` with `USAGE_MEDIA` for alarms, navigation prompts, or ringtones.
- Project **MUST NOT** keep audio rolling through a headphone unplug.

## Step 1: plan

1. Identify audio content class:
    - Music streaming: `USAGE_MEDIA` + `CONTENT_TYPE_MUSIC`.
    - Podcast or audiobook: `USAGE_MEDIA` + `CONTENT_TYPE_SPEECH`.
    - Video with audio: see `media3-video-playback` skill.
2. Enumerate all `ExoPlayer` constructions.
3. Decide how chapter metadata maps to `MediaMetadata` extras.
4. Confirm the notification controls align with the content type.
5. Plan volume boost.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
media3-session   = { module = "androidx.media3:media3-session",   version.ref = "media3" }
```

## Step 3: configure AudioAttributes

### RIGHT

```kotlin
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

val audioAttributes = AudioAttributes.Builder()
    .setUsage(C.USAGE_MEDIA)
    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
    .build()

val player = ExoPlayer.Builder(context)
    .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
    .setHandleAudioBecomingNoisy(true)
    .setWakeMode(C.WAKE_MODE_NETWORK)
    .build()
```

For spoken content (podcasts, audiobooks):

```kotlin
val spokenAttributes = AudioAttributes.Builder()
    .setUsage(C.USAGE_MEDIA)
    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
    .build()
```

### WRONG

```kotlin
// WRONG: default AudioAttributes do not trigger audio focus ducking during calls or notifications
val player = ExoPlayer.Builder(context).build()
```

## Step 4: surface metadata on MediaItem

### RIGHT

```kotlin
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

val item = MediaItem.Builder()
    .setMediaId("track-42")
    .setUri("https://example.com/track-42.m4a")
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle("Blue Note Sessions, Take 3")
            .setArtist("Shunnek Quartet")
            .setAlbumTitle("Dhaka After Dark")
            .setArtworkUri(Uri.parse("https://example.com/art/42.jpg"))
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
            .build()
    )
    .build()
```

## Step 5: chapters on a podcast or audiobook

Media3 1.10.0 does not have a dedicated chapters API. Use `MediaMetadata.setExtras` plus a list in a Bundle.

```kotlin
import android.os.Bundle

val chapters = Bundle().apply {
    putLongArray("chapters.startMs", longArrayOf(0, 600_000, 1_200_000))
    putStringArray("chapters.title", arrayOf("Intro", "Act 1", "Act 2"))
}
val item = MediaItem.Builder()
    .setUri(episodeUri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle("Episode 12: The Cable Fire")
            .setExtras(chapters)
            .build()
    )
    .build()
```

## Step 6: becoming-noisy and focus

Enabling `setHandleAudioBecomingNoisy(true)` auto-pauses on headphone unplug. Enabling `handleAudioFocus = true` auto-ducks on incoming calls.

**DO NOT** register a `BroadcastReceiver` for `AudioManager.ACTION_AUDIO_BECOMING_NOISY`. Media3 already handles it.

## Step 7: notification controls for audio-only

```kotlin
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.CommandButton

class PodcastPlayerService : MediaSessionService() {
    private lateinit var session: MediaSession

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(spokenAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        session = MediaSession.Builder(this, player)
            .setCustomLayout(
                listOf(
                    CommandButton.Builder(CommandButton.ICON_REWIND_15).build(),
                    CommandButton.Builder(CommandButton.ICON_FAST_FORWARD_30).build(),
                )
            )
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    override fun onDestroy() {
        session.player.release()
        session.release()
        super.onDestroy()
    }
}
```

## Step 8: playback speed for spoken content

```kotlin
player.setPlaybackSpeed(1.5f) // between 0.5f and 2.0f for speech
```

## Step 9: Bluetooth and Auto

`AudioAttributes` with `USAGE_MEDIA` automatically routes to the last connected A2DP sink.

## Step 10: volume

Media3 writes to `STREAM_MUSIC`. The hardware volume keys control this stream.

```kotlin
val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newLevel, 0)
```

**DO NOT** set `player.volume = 0.01f` as a "soft mute". Users cannot recover from it.

## Common pitfalls

- **Default `AudioAttributes`.**
- **Missing `handleAudioFocus`.**
- **Registering a custom becoming-noisy receiver.**
- **Using `USAGE_MEDIA` for alarms or navigation prompts.**
- **Treating audiobooks as music (`CONTENT_TYPE_MUSIC`).**
- **Custom volume scalar as mute.**
- **Playback speed above 2.5x on music.**
- **No `MediaMetadata.setArtworkUri`.**
