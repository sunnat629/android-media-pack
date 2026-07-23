package dev.sunnat629.androidmediaskill

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.buttons.NextButton
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.PreviousButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import kotlinx.coroutines.delay

private val skillItems = listOf(
    SkillItem(
        name = "streaming-media-architecture",
        domain = "Architecture",
        summary = "Production Android/KMP streaming architecture, repositories, offline-first data, and reels/feed playback.",
    ),
    SkillItem(
        name = "migrate-exoplayer-to-media3",
        domain = "Migration",
        summary = "Migrates legacy ExoPlayer 2.x packages to AndroidX Media3 1.10.1.",
    ),
    SkillItem(
        name = "migrate-xml-ui-to-compose",
        domain = "Migration",
        summary = "Migrates XML PlayerView media UI to Media3 Compose UI.",
    ),
    SkillItem(
        name = "media3-background-playback-service",
        domain = "Core",
        summary = "MediaSessionService, MediaController, notification, media buttons, and service lifetime.",
    ),
    SkillItem(
        name = "media3-lifecycle-state",
        domain = "Core",
        summary = "Lifecycle-aware MediaController, player state, saved position, process death, and release safety.",
    ),
    SkillItem(
        name = "media3-compose-ui-material3",
        domain = "UI",
        summary = "ContentFrame, PlayerSurface, controls, lifecycle-safe state, and Material3.",
    ),
    SkillItem(
        name = "media3-adaptive-compose-ui",
        domain = "UI",
        summary = "Responsive player UI across phones, tablets, foldables, large screens, orientation, and insets.",
    ),
    SkillItem(
        name = "media3-view-ui-player",
        domain = "UI",
        summary = "View-based PlayerView, XML player UI, lifecycle attach/release, and Compose interop.",
    ),
    SkillItem(
        name = "media3-tv-leanback-ui",
        domain = "Device",
        summary = "Android TV Leanback playback UI, D-pad focus, overscan-safe controls, and transport keys.",
    ),
    SkillItem(
        name = "media3-android-auto-media-surface",
        domain = "Device",
        summary = "Android Auto media sessions, browse trees, transport controls, and car UX restrictions.",
    ),
    SkillItem(
        name = "media3-xr-media-surface",
        domain = "Device",
        summary = "Android XR media surface planning, immersive playback, controller input, and fallback paths.",
    ),
    SkillItem(
        name = "media3-video-playback",
        domain = "UI",
        summary = "Video surfaces, aspect ratio, first frame, HDR, PiP, and feed-safe handoff.",
    ),
    SkillItem(
        name = "media3-hls-dash-adaptive-streaming",
        domain = "Streaming",
        summary = "HLS/DASH adaptive streaming, manifests, subtitles, live-vs-VOD behavior, and buffer policy.",
    ),
    SkillItem(
        name = "media3-live-streaming",
        domain = "Streaming",
        summary = "Live offset, DVR window, catch-up, reconnect, and behind-live-window recovery.",
    ),
    SkillItem(
        name = "media3-live-only-streaming",
        domain = "Streaming",
        summary = "Non-DVR live streams where seeking and resume do not exist.",
    ),
    SkillItem(
        name = "media3-vod-playback",
        domain = "Streaming",
        summary = "VOD items, resume position, playlists, chapters, thumbnails, and next-item preload.",
    ),
    SkillItem(
        name = "media3-audio-playback",
        domain = "Streaming",
        summary = "Audio attributes, audio focus, becoming-noisy, chapters, metadata, and session controls.",
    ),
    SkillItem(
        name = "media3-rtsp-playback",
        domain = "Protocols",
        summary = "RTSP camera feeds, LAN streams, buffering, reconnect policy, and credential-safe errors.",
    ),
    SkillItem(
        name = "media3-smoothstreaming-playback",
        domain = "Protocols",
        summary = "SmoothStreaming manifests, adaptive playback, fallbacks, and manifest error handling.",
    ),
    SkillItem(
        name = "media3-midi-playback",
        domain = "Protocols",
        summary = "MIDI playback dependency checks, runtime caveats, and generated-audio behavior.",
    ),
    SkillItem(
        name = "media3-datasources-networking",
        domain = "Delivery",
        summary = "HTTP DataSource, OkHttp/Cronet/HttpEngine, cache, headers, auth, and custom DataSource decisions.",
    ),
    SkillItem(
        name = "media3-bandwidth-abr",
        domain = "Delivery",
        summary = "Bandwidth estimation, ABR limits, load control, network constraints, and preload contention.",
    ),
    SkillItem(
        name = "media3-cast-integration",
        domain = "Delivery",
        summary = "CastPlayer, local-to-remote handoff, MediaRouteButton, and Cast session lifecycle.",
    ),
    SkillItem(
        name = "media3-workmanager-offline-ops",
        domain = "Core",
        summary = "WorkManager-backed offline operations, download constraints, retries, cleanup, and foreground handoff.",
    ),
    SkillItem(
        name = "media3-drm-widevine-setup",
        domain = "Protection",
        summary = "Widevine DRM, license headers, offline licenses, L1/L3, HDCP, and failure recovery.",
    ),
    SkillItem(
        name = "media3-ads-ima",
        domain = "Ads & analytics",
        summary = "Google IMA CSAI/SSAI ad insertion, AdViewProvider, companions, and ad telemetry.",
    ),
    SkillItem(
        name = "media3-analytics-telemetry",
        domain = "Ads & analytics",
        summary = "QoE telemetry: TTFF, rebuffer, dropped frames, ABR, preload hit/miss, and player errors.",
    ),
    SkillItem(
        name = "media3-inspector-metadata-thumbnails",
        domain = "Off-player",
        summary = "Inspector metadata, thumbnail/frame extraction, and container sample inspection without playback.",
    ),
    SkillItem(
        name = "media3-transformer-editing",
        domain = "Processing",
        summary = "Transformer editing and export jobs: trim, transcode, progress, cancellation, and cleanup.",
    ),
    SkillItem(
        name = "media3-video-effects-lottie-muxer",
        domain = "Processing",
        summary = "Video effects, Lottie overlays, muxing, export boundaries, and processing failures.",
    ),
    SkillItem(
        name = "media3-test-utils-robolectric",
        domain = "Testing",
        summary = "Media3 test utilities, Robolectric patterns, fake playback state, and realistic assertions.",
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT,
            ),
        )
        setContent {
            SampleAppTheme {
                val player = rememberSamplePlayer()
                SkillsHome(player = player)
            }
        }
    }
}

@Composable
private fun SampleAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF006B5F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFBFECE3),
            onPrimaryContainer = Color(0xFF00201C),
            secondary = Color(0xFF725B00),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFE08A),
            onSecondaryContainer = Color(0xFF221B00),
            tertiary = Color(0xFF485B92),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDDE1FF),
            onTertiaryContainer = Color(0xFF00164D),
            background = Color(0xFFFAFBF8),
            surface = Color(0xFFFAFBF8),
            surfaceVariant = Color(0xFFE0E5DF),
            onSurface = Color(0xFF181D1B),
            onSurfaceVariant = Color(0xFF404944),
            outline = Color(0xFF707973),
        ),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillsHome(player: Player?) {
    var showSkills by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Android Media Skill") },
                actions = {
                    IconButton(onClick = { showSkills = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = "Skills",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Hero(player = player)
            }
            item {
                SkillBrief()
            }
        }
    }

    if (showSkills) {
        SkillsInfoSheet(onDismiss = { showSkills = false })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun Hero(player: Player?) {
    val playback = rememberPlaybackSnapshot(player)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(playback.videoAspectRatio)
                .clip(RoundedCornerShape(8.dp)),
            color = Color(0xFF101715),
        ) {
            when {
                playback.errorMessage != null -> {
                    PlaybackError(
                        message = playback.errorMessage,
                        onRetry = { player?.prepare() },
                    )
                }
                player != null -> {
                    ContentFrame(
                        player = player,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Connecting player",
                            color = Color.White,
                        )
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = playback.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StreamChip(
                    label = "HLS",
                    selected = playback.streamType == StreamType.Hls,
                    onClick = { player?.seekToDefaultPosition(0) },
                )
                StreamChip(
                    label = "DASH",
                    selected = playback.streamType == StreamType.Dash,
                    onClick = { player?.seekToDefaultPosition(1) },
                )
            }
        }
        if (player != null) {
            PlaybackProgress(playback = playback)
            PlayerControls(player = player)
        }
    }
}

@Composable
private fun StreamChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = if (selected) "$label selected" else label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
    )
}

@Composable
private fun PlaybackProgress(playback: PlaybackSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(playback.bufferedFraction)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(playback.playedFraction)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(playback.positionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatTime(playback.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlaybackError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Playback failed",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun PlayerControls(player: Player) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviousButton(player = player)
        SeekBackButton(player = player)
        PlayPauseButton(player = player)
        SeekForwardButton(player = player)
        NextButton(player = player)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillBrief() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Skill coverage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MetricPill(value = "${skillItems.size}", label = "skills")
                MetricPill(value = "HLS", label = "live/VOD")
                MetricPill(value = "DASH", label = "adaptive")
                MetricPill(value = "Media3", label = "Compose")
            }
        }
    }
}

@Composable
private fun MetricPill(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillsInfoSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "${skillItems.size} Media3 skills",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(skillItems, key = { it.name }) { skill ->
                SkillCard(skill = skill)
            }
        }
    }
}

@Composable
private fun SkillCard(skill: SkillItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DomainDot(domain = skill.domain)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = skill.domain,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            HorizontalDivider()
            Text(
                text = skill.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DomainDot(domain: String) {
    val color = when (domain) {
        "Architecture" -> MaterialTheme.colorScheme.primary
        "Migration" -> MaterialTheme.colorScheme.secondary
        "Core" -> MaterialTheme.colorScheme.tertiary
        "UI" -> Color(0xFF9C4146)
        "Streaming" -> Color(0xFF006D3A)
        "Protocols" -> Color(0xFF0057A8)
        "Delivery" -> Color(0xFF805600)
        "Device" -> Color(0xFF345A00)
        "Protection" -> Color(0xFF5C4B8A)
        "Ads & analytics" -> Color(0xFF8B4A00)
        "Processing" -> Color(0xFF9A3412)
        "Testing" -> Color(0xFF5B5F00)
        else -> MaterialTheme.colorScheme.outline
    }

    Spacer(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun rememberSamplePlayer(): Player? {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var restoreState by rememberSaveable(stateSaver = PlaybackRestoreState.Saver) {
        mutableStateOf(PlaybackRestoreState())
    }
    var player by remember { mutableStateOf<Player?>(null) }
    DisposableEffect(context, lifecycle) {
        var released = false
        val exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItems(
                    sampleMediaItems(),
                    restoreState.mediaItemIndex,
                    restoreState.positionMs,
                )
                playWhenReady = restoreState.playWhenReady
                prepare()
            }
        player = exoPlayer

        fun saveRestoreState(playWhenReady: Boolean = exoPlayer.playWhenReady) {
            restoreState = PlaybackRestoreState(
                mediaItemIndex = exoPlayer.currentMediaItemIndex,
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                playWhenReady = playWhenReady,
            )
        }

        fun releasePlayer() {
            if (!released) {
                // ON_STOP already paused the player, so keep the playWhenReady
                // intent captured there instead of the post-pause value.
                saveRestoreState(playWhenReady = restoreState.playWhenReady)
                exoPlayer.release()
                released = true
                player = null
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Capture playWhenReady before pausing so rotation resumes playback.
                    saveRestoreState()
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_DESTROY -> releasePlayer()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            releasePlayer()
        }
    }
    return player
}

private data class PlaybackRestoreState(
    val mediaItemIndex: Int = 0,
    val positionMs: Long = 0L,
    val playWhenReady: Boolean = false,
) {
    companion object {
        val Saver: Saver<PlaybackRestoreState, Any> = listSaver(
            save = { listOf(it.mediaItemIndex, it.positionMs, it.playWhenReady) },
            restore = {
                PlaybackRestoreState(
                    mediaItemIndex = it[0] as Int,
                    positionMs = it[1] as Long,
                    playWhenReady = it[2] as Boolean,
                )
            },
        )
    }
}

private fun sampleMediaItems(): List<MediaItem> =
    listOf(
        MediaItem.Builder()
            .setMediaId("hls")
            .setUri("https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8")
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("HLS: Angel One")
                    .setArtist("Shaka demo assets")
                    .build(),
            )
            .build(),
        MediaItem.Builder()
            .setMediaId("dash")
            .setUri("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd")
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("DASH: Tears")
                    .setArtist("ExoPlayer demo sample")
                    .build(),
            )
            .build(),
    )

@Composable
private fun rememberPlaybackSnapshot(player: Player?): PlaybackSnapshot {
    var snapshot by remember(player) { mutableStateOf(player.toPlaybackSnapshot()) }
    DisposableEffect(player) {
        if (player == null) {
            return@DisposableEffect onDispose {}
        }
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                snapshot = player.toPlaybackSnapshot()
            }

            override fun onEvents(player: Player, events: Player.Events) {
                snapshot = player.toPlaybackSnapshot()
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    // Position/buffer only advance smoothly while playing; every other field is
    // updated by the Player.Listener above.
    LaunchedEffect(player, snapshot.isPlaying) {
        while (player != null && snapshot.isPlaying) {
            snapshot = player.toPlaybackSnapshot()
            delay(500)
        }
    }
    return snapshot
}

private fun Player?.toPlaybackSnapshot(): PlaybackSnapshot {
    if (this == null) {
        return PlaybackSnapshot()
    }

    val resolvedDurationMs = duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L
    val mediaId = currentMediaItem?.mediaId.orEmpty()
    val currentTitle = currentMediaItem
        ?.mediaMetadata
        ?.title
        ?.toString()
        ?: "HLS + DASH sample"
    val size = videoSize
    val aspectRatio = if (size.height > 0 && size.width > 0) {
        size.width * size.pixelWidthHeightRatio / size.height
    } else {
        16f / 9f
    }

    return PlaybackSnapshot(
        title = currentTitle,
        streamType = StreamType.fromMediaId(mediaId),
        positionMs = currentPosition.coerceAtLeast(0L),
        durationMs = resolvedDurationMs,
        bufferedMs = bufferedPosition.coerceAtLeast(0L),
        isPlaying = isPlaying,
        videoAspectRatio = aspectRatio,
        errorMessage = playerError?.let { error ->
            error.localizedMessage ?: error.errorCodeName
        },
    )
}

private fun formatTime(timeMs: Long): String {
    if (timeMs <= 0) {
        return "0:00"
    }
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private data class PlaybackSnapshot(
    val title: String = "Connecting player",
    val streamType: StreamType = StreamType.Unknown,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val isPlaying: Boolean = false,
    val videoAspectRatio: Float = 16f / 9f,
    val errorMessage: String? = null,
) {
    val playedFraction: Float
        get() = progressFraction(positionMs)

    val bufferedFraction: Float
        get() = progressFraction(bufferedMs)

    private fun progressFraction(value: Long): Float {
        if (durationMs <= 0L) {
            return 0f
        }
        return (value.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }
}

private enum class StreamType {
    Hls,
    Dash,
    Unknown;

    companion object {
        fun fromMediaId(mediaId: String): StreamType =
            when (mediaId) {
                "hls" -> Hls
                "dash" -> Dash
                else -> Unknown
            }
    }
}

private data class SkillItem(
    val name: String,
    val domain: String,
    val summary: String,
)
