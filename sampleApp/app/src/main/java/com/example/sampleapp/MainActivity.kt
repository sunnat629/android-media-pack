package com.example.sampleapp

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.compose.material3.ContentFrame
import androidx.media3.ui.compose.material3.PlayPauseButton
import androidx.media3.ui.compose.material3.SeekBackButton
import androidx.media3.ui.compose.material3.SeekForwardButton
import com.example.sampleapp.playback.PlaybackService
import com.google.common.util.concurrent.MoreExecutors

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
        enableEdgeToEdge()
        setContent {
            SampleAppTheme {
                val player = rememberMediaController()
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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Android Media Skills") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Hero(player = player)
            }
            item {
                DomainSummary()
            }
            items(skillItems, key = { it.name }) { skill ->
                SkillCard(skill = skill)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun Hero(player: Player?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Media3 1.10.1 skill pack",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "A focused home for Android and KMP media work: architecture, playback, streaming, adaptive UI, device surfaces, DRM, ads, telemetry, editing, and tests.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFF101715),
            ) {
                if (player != null) {
                    ContentFrame(
                        player = player,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Connecting MediaController",
                            color = Color.White,
                        )
                    }
                }
            }
            if (player != null) {
                PlayerControls(player = player)
            }
        }
    }
}

@Composable
private fun PlayerControls(player: Player) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SeekBackButton(player = player)
        PlayPauseButton(player = player)
        SeekForwardButton(player = player)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomainSummary() {
    val domains = skillItems
        .groupBy { it.domain }
        .map { (domain, skills) -> domain to skills.size }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${skillItems.size} skills",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                domains.forEach { (domain, count) ->
                    AssistChip(
                        onClick = {},
                        label = { Text(text = "$domain $count") },
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillCard(skill: SkillItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
private fun rememberMediaController(): Player? {
    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }
    DisposableEffect(context) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            player = future.get()
        }, MoreExecutors.directExecutor())
        onDispose {
            (player as? MediaController)?.release()
            player = null
        }
    }
    return player
}

private data class SkillItem(
    val name: String,
    val domain: String,
    val summary: String,
)
