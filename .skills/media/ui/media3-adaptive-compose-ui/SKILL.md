---
name: media3-adaptive-compose-ui
description: "Compact skill for adaptive Media3 Compose UI across phones, tablets, foldables, large screens, orientation changes, and system insets."
license: Apache-2.0
metadata:
  author: Shunnek Labs
  version: "1.1"
  target_media3_version: "1.10.1"
  last_reviewed: "2026-07-23"
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
- Read the size class from `currentWindowAdaptiveInfo().windowSizeClass` (material3-adaptive) and branch with `isWidthAtLeastBreakpoint` checks against the `WindowSizeClass` bound constants, not fixed phone breakpoints.
- The `WindowWidthSizeClass`/`WindowHeightSizeClass` bucket enums are deprecated in androidx.window 1.4; **DO NOT** add new code that switches on them.
- Reserve safe space for status bars, navigation bars, cutouts, hinges, and IME.
- On tablets/foldables, consider two-pane media-detail layouts, playlist rails, or persistent queue panels.
- Keep accessibility labels, touch targets, focus order, and keyboard/D-pad traversal valid across sizes.

## Example

```kotlin
val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
when {
    windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
    ) -> ExpandedPlayerScaffold()
    windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    ) -> MediumPlayerScaffold()
    else -> CompactPlayerScaffold()
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
