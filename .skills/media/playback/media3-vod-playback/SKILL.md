---
name: media3-vod-playback
description: Use this skill to ship video-on-demand (VOD) playback with AndroidX Media3 1.10.0. Use this skill to build a static VOD MediaItem, persist resume position across sessions, configure a playlist with auto-advance, expose chapters and scrubber thumbnails, prefetch the next item with DefaultPreloadManager, and distinguish continue-watching from start-over flows.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - vod
    - on-demand
    - resume
    - playlist
    - preloadmanager
    - thumbnails
    - chapters

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** persist resume positions in a single repository.
- Project **MUST NOT** mix live and VOD items in the same `Timeline`.
- Project **MUST NOT** seek to a resume position before `STATE_READY`.

## Step 1: plan

1. Enumerate every VOD feed.
2. Define "continue watching" rules.
3. Decide playlist autoadvance.
4. Plan thumbnail source.
5. Confirm preload budget.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer      = { module = "androidx.media3:media3-exoplayer",      version.ref = "media3" }
media3-exoplayer-hls  = { module = "androidx.media3:media3-exoplayer-hls",  version.ref = "media3" }
media3-exoplayer-dash = { module = "androidx.media3:media3-exoplayer-dash", version.ref = "media3" }
```

## Step 3: build a VOD MediaItem

### RIGHT

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

val vodItem = MediaItem.Builder()
    .setMediaId("episode-s1e3")
    .setUri("https://example.com/s1e3/master.m3u8")
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle("Season 1, Episode 3: The Cable Fire")
            .setDisplayTitle("The Cable Fire")
            .setReleaseYear(2026)
            .setMediaType(MediaMetadata.MEDIA_TYPE_TV_SHOW)
            .build()
    )
    .build()

player.setMediaItem(vodItem)
player.prepare()
```

### WRONG

```kotlin
// WRONG: using a LiveConfiguration on a VOD item forces the ABR to chase a default live offset
val vodItem = MediaItem.Builder()
    .setUri(url)
    .setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build())
    .build()
```

## Step 4: persist resume positions

```kotlin
data class ResumeEntry(val mediaId: String, val positionMs: Long, val durationMs: Long, val updatedAt: Long)

class ResumeRepository(private val dao: ResumeDao) {
    fun shouldResume(entry: ResumeEntry): Boolean {
        if (entry.positionMs < 30_000) return false
        if (entry.durationMs > 0 && entry.durationMs - entry.positionMs < 60_000) return false
        if (System.currentTimeMillis() - entry.updatedAt > 90L * 86_400_000) return false
        return true
    }
}
```

## Step 5: seek to resume after STATE_READY

```kotlin
player.addListener(object : Player.Listener {
    override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_READY && !hasResumed) {
            val entry = resumeStore.fetch(player.currentMediaItem?.mediaId ?: return)
            if (entry != null && resumeRepository.shouldResume(entry)) {
                player.seekTo(entry.positionMs)
            }
            hasResumed = true
        }
    }
})
```

## Step 6: playlist autoadvance

```kotlin
player.setMediaItems(
    listOf(item1, item2, item3),
    /* startIndex = */ 0,
    /* startPositionMs = */ 0L,
)
player.repeatMode = Player.REPEAT_MODE_OFF
player.prepare()
```

## Step 7: chapters and scrubber thumbnails

For scrubber thumbnails, use the HLS I-FRAME playlist if the server provides one. Media3 1.10.0 auto-discovers I-FRAME tracks in HLS master playlists.

## Step 8: preload the next item

```kotlin
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager

val preloadManager = DefaultPreloadManager.Builder(MediaSingletons.bandwidthMeter)
    .setContext(context)
    .build()

preloadManager.add(nextItem, /* rank = */ 1)
```

## Step 9: start-over vs continue-watching UI

If `shouldResume(entry)` returns true, show "Resume at 12:34" and "Start over". Else show "Play".

## Step 10: rate limit resume writes

```kotlin
private val resumeDebouncer = Debouncer(5_000L)

override fun onIsPlayingChanged(isPlaying: Boolean) {
    if (isPlaying) resumeDebouncer.start { saveResume() }
    else { resumeDebouncer.cancel(); saveResume() }
}
```

## Common pitfalls

- **Mixing live and VOD in one Timeline.**
- **Using `LiveConfiguration` on VOD.**
- **Seeking before STATE_READY.**
- **Writing resume on every position tick.**
- **Preloading on cellular.**
- **Silent auto-resume with no UI cue.**
- **On-device thumbnail synthesis.**
- **Duplicate resume stores.**
