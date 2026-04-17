---
name: media3-inspector-metadata-thumbnails
description: Use this skill to read media metadata, extract thumbnails, and demux containers without instantiating an ExoPlayer, using the AndroidX Media3 1.10.0 media3-inspector module. Use this skill to replace android.media.MediaMetadataRetriever with MetadataRetriever, extract frames with FrameExtractor, and read container samples with MediaExtractorCompat off the main thread.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - inspector
    - metadataretriever
    - frameextractor
    - mediaextractorcompat
    - thumbnails
    - off-main-thread

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Inspector calls **MUST NOT** run on the main thread.
- Project **MUST NOT** use `android.media.MediaMetadataRetriever` in the RIGHT path for new code.

## Step 1: plan

1. Grep the codebase for `android.media.MediaMetadataRetriever`.
2. Flag every place that reads a frame via `MediaMetadataRetriever.getFrameAtTime`.
3. Flag every custom `MediaExtractor` usage that parses samples outside of a player.
4. Confirm all inspection happens off the main thread.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-inspector = { module = "androidx.media3:media3-inspector", version.ref = "media3" }
```

## Step 3: read metadata with MetadataRetriever

### RIGHT

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.inspector.MetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadDuration(context: Context, uri: String): Long = withContext(Dispatchers.IO) {
    val item = MediaItem.fromUri(uri)
    MetadataRetriever.Builder(context, item).build().use { retriever ->
        retriever.retrieveDurationUs().await() / 1_000
    }
}
```

### WRONG

```kotlin
// WRONG: platform MediaMetadataRetriever on the main thread freezes the UI on slow files
val mmr = android.media.MediaMetadataRetriever()
mmr.setDataSource(uri)
val durationMs = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
```

## Step 4: extract a thumbnail with FrameExtractor

### RIGHT

```kotlin
import android.graphics.Bitmap
import androidx.media3.common.MediaItem
import androidx.media3.inspector.FrameExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun thumbnail(context: Context, uri: String, atMicros: Long): Bitmap =
    withContext(Dispatchers.IO) {
        val item = MediaItem.fromUri(uri)
        FrameExtractor.Builder(context, item).build().use { extractor ->
            extractor.getFrame(atMicros).await().bitmap
        }
    }
```

### WRONG

```kotlin
// WRONG: platform retriever blocks the calling thread and can OOM on 4K frames
val bitmap = android.media.MediaMetadataRetriever().run {
    setDataSource(uri)
    getFrameAtTime(atMicros, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
}
```

## Step 5: demux samples with MediaExtractorCompat

### RIGHT

```kotlin
import androidx.media3.common.Format
import androidx.media3.inspector.MediaExtractorCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun firstVideoFormat(context: Context, uri: String): Format? = withContext(Dispatchers.IO) {
    MediaExtractorCompat(context).use { extractor ->
        extractor.setDataSource(uri)
        (0 until extractor.trackCount)
            .map(extractor::getTrackFormat)
            .firstOrNull { it.sampleMimeType?.startsWith("video/") == true }
    }
}
```

## Step 6: batched metadata reads

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

suspend fun loadDurations(context: Context, uris: List<String>): Map<String, Long> = coroutineScope {
    val gate = Semaphore(permits = 4)
    uris.associateWith { uri ->
        async {
            gate.acquire()
            try { loadDuration(context, uri) } finally { gate.release() }
        }
    }.mapValues { (_, deferred) -> deferred.await() }
}
```

## Step 7: release resources deterministically

Every inspector returned by the builders is `Closeable`. Use `.use { ... }` in Kotlin.

## Step 8: integrate with MediaSession

```kotlin
import androidx.media3.common.MediaMetadata

fun buildSessionMetadata(title: String, artistOrAuthor: String, artwork: Bitmap?): MediaMetadata =
    MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artistOrAuthor)
        .apply { if (artwork != null) setArtworkData(encodeJpeg(artwork), MediaMetadata.PICTURE_TYPE_FRONT_COVER) }
        .build()
```

## Common pitfalls

- **Calling the inspector on the main thread.**
- **Forgetting `.use { ... }`.**
- **Unbounded parallelism in batch jobs.**
- **Treating `getFrame` output as a pooled bitmap.**
- **Mixing `android.media.MediaMetadataRetriever` with the new retriever in hot paths.**
- **Expecting `MediaExtractorCompat` to work on encrypted content without DRM provisioning.**
