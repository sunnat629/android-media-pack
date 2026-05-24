---
name: media3-adaptive-compose-ui
description: "Compact skill for adaptive Media3 Compose UI across phones, tablets, foldables, large screens, orientation changes, and system insets."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.0"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-05-24"
  keywords:
    - android
    - media3
    - compose
    - adaptive
    - tablet
    - foldable
    - large-screen
---

## Trigger

Use for responsive player UI across mobile, tablet, foldable, landscape, split-screen, and large-screen layouts.

## Rules

- Start with `media3-compose-ui-material3` for player surface and controls.
- Pin Media3 to `1.10.1` through the version catalog.
- Keep video surface dimensions stable across state changes; controls must not resize content unexpectedly.
- Use compact/medium/expanded layout decisions, not fixed phone breakpoints.
- Reserve safe space for status bars, navigation bars, cutouts, hinges, and IME.
- On tablets/foldables, consider two-pane media-detail layouts, playlist rails, or persistent queue panels.
- Keep accessibility labels, touch targets, focus order, and keyboard/D-pad traversal valid across sizes.

## Example

```kotlin
when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact -> CompactPlayerScaffold()
    WindowWidthSizeClass.Medium -> MediumPlayerScaffold()
    WindowWidthSizeClass.Expanded -> ExpandedPlayerScaffold()
}
```

## Do Not

- Do not scale text with viewport width.
- Do not let controls overlap system bars or video subtitles.
- Do not use the phone layout unchanged on tablets or foldables.

## Related

- `media3-compose-ui-material3`
- `media3-video-playback`
- `migrate-xml-ui-to-compose`
