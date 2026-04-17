# Package mapping: ExoPlayer 2.x to Media3

Use this table when rewriting imports. Prefer a scripted rewrite (see `rewrite-imports.sh`) over manual edits.

| ExoPlayer 2.x (`com.google.android.exoplayer2.*`) | Media3 (`androidx.media3.*`) |
| --- | --- |
| `com.google.android.exoplayer2.SimpleExoPlayer` | `androidx.media3.exoplayer.ExoPlayer` |
| `com.google.android.exoplayer2.ExoPlayer` | `androidx.media3.exoplayer.ExoPlayer` |
| `com.google.android.exoplayer2.Player` | `androidx.media3.common.Player` |
| `com.google.android.exoplayer2.MediaItem` | `androidx.media3.common.MediaItem` |
| `com.google.android.exoplayer2.Format` | `androidx.media3.common.Format` |
| `com.google.android.exoplayer2.C` | `androidx.media3.common.C` |
| `com.google.android.exoplayer2.Timeline` | `androidx.media3.common.Timeline` |
| `com.google.android.exoplayer2.Tracks` | `androidx.media3.common.Tracks` |
| `com.google.android.exoplayer2.trackselection.*` | `androidx.media3.exoplayer.trackselection.*` |
| `com.google.android.exoplayer2.ui.StyledPlayerView` | `androidx.media3.ui.PlayerView` |
| `com.google.android.exoplayer2.ui.PlayerView` | `androidx.media3.ui.PlayerView` |
| `com.google.android.exoplayer2.ui.SubtitleView` | `androidx.media3.ui.SubtitleView` |
| `com.google.android.exoplayer2.upstream.DefaultHttpDataSource` | `androidx.media3.datasource.DefaultHttpDataSource` |
| `com.google.android.exoplayer2.upstream.cache.SimpleCache` | `androidx.media3.datasource.cache.SimpleCache` |
| `com.google.android.exoplayer2.upstream.cache.CacheDataSource` | `androidx.media3.datasource.cache.CacheDataSource` |
| `com.google.android.exoplayer2.source.DefaultMediaSourceFactory` | `androidx.media3.exoplayer.source.DefaultMediaSourceFactory` |
| `com.google.android.exoplayer2.source.hls.HlsMediaSource` | `androidx.media3.exoplayer.hls.HlsMediaSource` |
| `com.google.android.exoplayer2.source.dash.DashMediaSource` | `androidx.media3.exoplayer.dash.DashMediaSource` |
| `com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource` | `androidx.media3.exoplayer.smoothstreaming.SsMediaSource` |
| `com.google.android.exoplayer2.source.rtsp.RtspMediaSource` | `androidx.media3.exoplayer.rtsp.RtspMediaSource` |
| `com.google.android.exoplayer2.drm.DefaultDrmSessionManager` | `androidx.media3.exoplayer.drm.DefaultDrmSessionManager` |
| `com.google.android.exoplayer2.drm.DefaultDrmSessionManagerProvider` | `androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider` |
| `com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector` | Removed. Use `androidx.media3.session.MediaSession`. |
| `com.google.android.exoplayer2.ui.PlayerNotificationManager` | Removed. Notification is emitted by `androidx.media3.session.MediaSessionService`. |
| `com.google.android.exoplayer2.ext.cast.CastPlayer` | `androidx.media3.cast.CastPlayer` (rewritten API, `setLocalPlayer`) |
| `com.google.android.exoplayer2.ext.ima.ImaAdsLoader` | `androidx.media3.exoplayer.ima.ImaAdsLoader` |

Any class under `com.google.android.exoplayer2.ext.*` that is not listed here is either removed or folded into a core Media3 module. Confirm against the Media3 release notes before assuming a one-to-one replacement exists.
