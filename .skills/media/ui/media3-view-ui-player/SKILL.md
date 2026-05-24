---
name: media3-view-ui-player
description: "Compact skill for Media3 View-based PlayerView UI, XML migration boundaries, lifecycle attach/release, and Compose interop."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - playerView
    - views
    - xml
    - compose-interop
---

## Trigger

Use for Android Views playback UI, XML `PlayerView`, ViewBinding screens, or Compose screens that must host existing Views.

## Rules

- Prefer `media3-compose-ui-material3` for new Compose-first screens.
- Pin Media3 to `1.10.1` through the version catalog.
- Use `androidx.media3.ui.PlayerView`; do not use legacy ExoPlayer UI packages.
- Attach the player when the screen is active and clear it when the View is destroyed.
- Keep player ownership outside fragments/views unless the screen truly owns playback lifetime.
- Keep system inset and fullscreen handling explicit; do not let controls overlap bars.
- Use `AndroidView` interop only when migration cost or platform UI needs justify it.

## Example

```toml
media3-ui = { module = "androidx.media3:media3-ui", version.ref = "media3" }
```

```xml
<androidx.media3.ui.PlayerView
    android:id="@+id/playerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Do Not

- Do not mix `com.google.android.exoplayer2.ui.PlayerView` with Media3.
- Do not create or release the service-owned player from a View.
- Do not use View interop as the default for a new Compose-only screen.

## Related

- `migrate-xml-ui-to-compose`
- `media3-compose-ui-material3`
- `media3-video-playback`
