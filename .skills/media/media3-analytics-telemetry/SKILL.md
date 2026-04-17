---
name: media3-analytics-telemetry
description: Use this skill to collect playback analytics and telemetry from an AndroidX Media3 1.10.0 player. Use this skill to register an AnalyticsListener before load, gather on-device summaries via PlaybackStatsListener, extract derived metrics (average bitrate, dropped frames, rebuffering time, join latency), correlate StuckPlayerException and ABR switches, and optionally bridge to Mux Data, Bitmovin Analytics, or FastPix SDKs.
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.0"
  last_reviewed: "2026-04-17"
  keywords:
    - android
    - media3
    - analytics
    - analyticslistener
    - playbackstatslistener
    - playbackstats
    - mux
    - bitmovin
    - telemetry

---

## Prerequisites

- Project **MUST** use `minSdk` 21 or later.
- Project **MUST** pin Media3 to **1.10.0** or later.
- Project **MUST** register its `AnalyticsListener` **before** load.
- Project **MUST NOT** hold an `AnalyticsListener` that captures an activity reference.

## Step 1: plan

1. Enumerate required metrics.
2. Decide the delivery mechanism.
3. Confirm the analytics pipeline can absorb events at high rates.
4. Identify session boundaries.
5. Flag any manual `Player.Listener` usage that duplicates `AnalyticsListener` events.

## Step 2: Gradle dependencies

```toml
[versions]
media3 = "1.10.0"

[libraries]
media3-exoplayer = { module = "androidx.media3:media3-exoplayer", version.ref = "media3" }
```

## Step 3: register AnalyticsListener before load

### RIGHT

```kotlin
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.exoplayer.analytics.PlaybackStats

val statsListener = PlaybackStatsListener(/* keepHistory = */ false) { eventTime, stats ->
    telemetry.emitSessionEnd(stats, eventTime)
}

val player = ExoPlayer.Builder(context).build().also { exo ->
    exo.addAnalyticsListener(statsListener)
    exo.addAnalyticsListener(object : AnalyticsListener {
        override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: androidx.media3.common.PlaybackException) {
            telemetry.emitError(eventTime, error)
        }
    })
}

player.setMediaItem(mediaItem)
player.prepare()
```

### WRONG

```kotlin
// WRONG: registering AnalyticsListener after prepare() loses the initial render window
player.setMediaItem(mediaItem)
player.prepare()
player.addAnalyticsListener(statsListener)
```

## Step 4: PlaybackStats summary metrics

| Field | Meaning |
|---|---|
| `totalPlayTimeMs` | Foreground playtime, excluding ads |
| `totalAdPlayTimeMs` | Ad playback time |
| `totalRebufferTimeMs` | Time spent rebuffering |
| `totalPauseBufferTimeMs` | Buffering while paused |
| `meanTimeBetweenRebuffers()` | Mean time between rebuffer events |
| `droppedFrames` | Count of dropped video frames |
| `videoFormatHeightTimeProduct` | Sum of (resolution height * time) for a weighted avg resolution |

## Step 5: custom AnalyticsListener for per-event telemetry

```kotlin
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.exoplayer.analytics.AnalyticsListener

val customListener = object : AnalyticsListener {
    override fun onVideoInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: androidx.media3.exoplayer.DecoderReuseEvaluation?,
    ) {
        telemetry.trackAbrSwitch(
            width = format.width,
            height = format.height,
            bitrate = format.bitrate,
            positionMs = eventTime.currentPlaybackPositionMs,
        )
    }

    override fun onDroppedVideoFrames(
        eventTime: AnalyticsListener.EventTime,
        droppedFrames: Int,
        elapsedMs: Long,
    ) {
        telemetry.trackDroppedFrames(droppedFrames, elapsedMs)
    }
}
player.addAnalyticsListener(customListener)
```

## Step 6: correlate StuckPlayerException

Media3 1.10.0 emits `StuckPlayerException` when the player stalls without progress.

```kotlin
import androidx.media3.exoplayer.StuckPlayerException

override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: androidx.media3.common.PlaybackException) {
    when (error) {
        is StuckPlayerException -> telemetry.trackStuck(eventTime, error)
        else -> telemetry.trackError(eventTime, error)
    }
}
```

## Step 7: derive join latency

```kotlin
import androidx.media3.exoplayer.analytics.AnalyticsListener

class JoinLatencyTracker : AnalyticsListener {
    private var prepareWallClockMs: Long = 0

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
        if (reason == androidx.media3.common.Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
            prepareWallClockMs = eventTime.realtimeMs
        }
    }

    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        if (prepareWallClockMs > 0) {
            val join = eventTime.realtimeMs - prepareWallClockMs
            telemetry.trackJoinLatency(join)
            prepareWallClockMs = 0
        }
    }
}
```

## Step 8: third-party SDK bridges

- Mux Data: `MuxStatsExoPlayer(muxData, player, env, customerVideoData, customerViewData)`
- Bitmovin: `ExoPlayerCollector(player, BitmovinAnalyticsConfig(...))`
- FastPix: `FastpixMetrics.monitor(player, CustomerData(...))`

## Step 9: dispose cleanly

```kotlin
override fun onDestroy() {
    muxStats?.release()
    player.release()
    super.onDestroy()
}
```

## Common pitfalls

- **Registering the listener after `prepare()`.**
- **Activity reference captured by the listener.**
- **Mixing multiple third-party SDKs that own session boundaries.**
- **Measuring join latency from `setMediaItem` to `STATE_READY`.**
- **Duplicating `Player.Listener` and `AnalyticsListener` for the same event.**
- **Not handling `StuckPlayerException` distinctly.**
- **No session ID.**
- **Draining `PlaybackStats.playbackStateDurationsMs` without weighting by session.**
