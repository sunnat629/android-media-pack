# Trusted references

Curated list of trusted sources consulted when authoring and auditing skills in this pack. When a skill cites an API, the citation **MUST** resolve to one of the sources below or to the current Media3 release notes. Community posts are allowed sparingly and marked clearly.

## Official sources (canonical)

- AndroidX Media3 release notes: https://developer.android.com/jetpack/androidx/releases/media3
- Media3 overview: https://developer.android.com/media/media3
- ExoPlayer docs index: https://developer.android.com/media/media3/exoplayer
- Build a media playback app: https://developer.android.com/media/implement/playback-app
- Background an audio app: https://developer.android.com/media/media3/session/background-playback
- Live streaming: https://developer.android.com/media/media3/exoplayer/live-streaming
- HLS streams: https://developer.android.com/media/media3/exoplayer/hls
- DRM in Media3: https://developer.android.com/media/media3/exoplayer/drm
- Network stacks: https://developer.android.com/media/media3/exoplayer/network-stacks
- Ad insertion: https://developer.android.com/media/media3/exoplayer/ad-insertion
- Analytics: https://developer.android.com/media/media3/exoplayer/analytics
- Migration guide: https://developer.android.com/media/media3/exoplayer/migration-guide
- Compose UI for Media3: https://developer.android.com/media/media3/ui/compose
- Android Developers Blog, Media3 1.9.0 announcement: https://android-developers.googleblog.com/2025/12/media3-190-whats-new.html
- Android Developers Blog, Media3 PreloadManager deep dive: https://android-developers.googleblog.com/2025/09/a-deep-dive-into-media3-preloadmanager.html
- **androidx/media** (canonical source): https://github.com/androidx/media
- androidx/media issue tracker: https://github.com/androidx/media/issues
- IMA DAI for ExoPlayer: https://developers.google.com/ad-manager/dynamic-ad-insertion/sdk/android/get-started/exoplayer-extension
- IMA CSAI for ExoPlayer: https://developers.google.com/interactive-media-ads/docs/sdks/android/client-side/get-started/exoplayer-extension
- Widevine DRM overview: https://developers.google.com/widevine/drm/overview
- Foreground services (Android 14+): https://developer.android.com/develop/background-work/services/foreground-services
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: https://developer.android.com/reference/android/Manifest.permission#FOREGROUND_SERVICE_MEDIA_PLAYBACK

### Jetpack Compose migration

- Migrate XML View to Jetpack Compose (Android Skills): https://developer.android.com/agents/skills/jetpack-compose/migration/migrate-xml-views-to-jetpack-compose/skill
- Migrating to Jetpack Compose codelab: https://developer.android.com/codelabs/jetpack-compose-migration
- Migration strategy: https://developer.android.com/develop/ui/compose/migrate/strategy

## Reference repositories (production-grade implementations)

These are production or production-adjacent projects that are safe to consult for patterns. Cross-check every citation against the canonical Media3 docs before adopting. Older projects may still reference `com.google.android.exoplayer2.*`, treat those as migration targets not as RIGHT patterns.

### Google and community-curated skills

- **android/skills** — Google's canonical skills repository. Canonical layout, anatomy, and examples for agent skills.
  https://github.com/android/skills

### Media3 and ExoPlayer reference apps

- **anilbeesetti/NextPlayer** — Modern Android video player built on Media3. Jetpack Compose, multi-module architecture, subtitle rendering, audio tracks, PiP, video library indexing.
  https://github.com/anilbeesetti/nextplayer
- **maxrave-dev/SimpMusic** — YouTube Music client built on Media3 `MediaSessionService`. Background audio, lock-screen transport, playlists, notification handling in Compose.
  https://github.com/maxrave-dev/SimpMusic
- **CarGuo/GSYVideoPlayer** — Long-running Chinese-community Android video player. Custom renderer trees, gesture overlays, floating window playback. Legacy ExoPlayer patterns live here, cross-check against Media3 1.9.0.
  https://github.com/CarGuo/GSYVideoPlayer
- **Doikki/DKVideoPlayer** — Lightweight Android video player with a customizable controller. Compact player UI decomposition.
  https://github.com/Doikki/DKVideoPlayer
- **brianwernick/ExoMedia** — Mature ExoPlayer wrapper library. Reference for a lightweight high-level API over the low-level Media3 primitives. Useful as a pattern, not a dependency.
  https://github.com/brianwernick/ExoMedia
- **yangchaojiang/yjPlay** — Video player library (Chinese community). Legacy ExoPlayer, reference for player skin / controller decomposition.
  https://github.com/yangchaojiang/yjPlay
- **MasayukiSuda/ExoPlayerFilter** — ExoPlayer integration with GPUImage-style GL filters. Reference for custom `GlEffect` chains and video filter pipelines.
  https://github.com/MasayukiSuda/ExoPlayerFilter
- **bluemeanie2-3/KidTube** — YouTube-like kids app. Reference for content gating, parental controls, and kid-safe playback patterns over Media3.
  https://github.com/bluemeanie2-3/KidTube

### Music / audio app references

- **Hamza417/Felicity** — Offline-first music player. Reference for library indexing, metadata, lock-screen transport, and service lifecycle.
  https://github.com/Hamza417/Felicity
- **caiyonglong/MusicLake** — Multi-source music player (online + offline). Reference for aggregating audio sources behind a single `Player`.
  https://github.com/caiyonglong/MusicLake
- **ZahraHeydari/MusicPlayer** — Compact music player reference. Reference for minimal `MediaSession` + service wiring.
  https://github.com/ZahraHeydari/MusicPlayer
- **rychardsonaguar-art/qiaomu-music-player-ncm** — NCM-format music player. Reference for custom decoder integration and proprietary audio format handling.
  https://github.com/rychardsonaguar-art/qiaomu-music-player-ncm
- **rawnaldclark/Stash** — Personal media stash / player. Reference for local media discovery patterns.
  https://github.com/rawnaldclark/Stash

### Multiplatform references

- **SEAbdulbasit/MusicApp-KMP** — Kotlin Multiplatform music app. Reference for separating shared business logic from the Android-specific Media3 playback layer.
  https://github.com/SEAbdulbasit/MusicApp-KMP

### Lifecycle and process death

- **jsericksk/Simple-Player** — Compose + Media3 reference app focused on lifecycle and system-initiated process death recovery.
  https://github.com/jsericksk/Simple-Player

## Community posts (use sparingly)

Community posts change over time and **MUST NOT** be cited in a skill's RIGHT example without a canonical source as the primary reference.

- Media3 1.9.0 "What's New" blog: https://android-developers.googleblog.com/2025/12/media3-190-whats-new.html
- Media3 1.8.0 "What's New" (Toni Heidenreich): https://medium.com/google-exoplayer/media3-1-8-0-whats-new-b857435651b9
- Low-latency live streaming with ExoPlayer (Toni Heidenreich): https://medium.com/google-exoplayer/low-latency-live-streaming-with-exoplayer-8552d5841060
- Simplified bandwidth meter usage: https://medium.com/google-exoplayer/simplified-bandwidth-meter-usage-17d8189f978b
- From AndroidView to PlayerSurface (Ioannis Anifantakis): https://proandroiddev.com/from-androidview-to-playersurface-modernizing-exoplayer-with-media3s-compose-ui-74e40ce81f94
- Using Media3 with Jetpack Compose (Ali Khorasani): https://medium.com/@khorassani64/using-media3-with-kotlin-jetpack-compose-f1c033acd016
- Getting started with Media3 UI Compose: https://proandroiddev.com/getting-started-with-media3-ui-compose-compose-uis-for-media-playback-7b634b9309b9
- Mastering playback state with ExoPlayer (Siva Ganesh Kantamani): https://proandroiddev.com/mastering-playback-state-with-exo-player-977016aa5003
- Handling process death (Alexander Gherschon): https://galex.dev/posts/how-to-solve-process-death-issues/

## Monitoring and analytics SDKs

- Mux Data SDK for Media3: https://www.mux.com/docs/guides/monitor-androidx-media3
- Bitmovin Analytics Android Collector: https://developer.bitmovin.com/playback/docs/setup-analytics-android-v3
- FastPix Data SDK for Media3: https://docs.fastpix.io/docs/monitor-androidx-media3

## Source of truth policy

- Primary: Media3 release notes and `androidx/media` source.
- Secondary: developer.android.com guides listed above.
- Tertiary: reference repos listed above, with drift checked.
- Community posts: never a primary citation in a skill's RIGHT path.
- If a community post contradicts the release notes, trust the release notes and file a `type-docs` Issue against the contradicting source.
