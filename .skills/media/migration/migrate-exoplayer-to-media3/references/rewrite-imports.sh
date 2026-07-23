#!/usr/bin/env bash
# rewrite-imports.sh
#
# Minimal helper to rewrite ExoPlayer 2.x imports to Media3.
#
# RECOMMENDED: use the official media3-migration.sh script from the
# androidx/media repository (https://github.com/androidx/media) as the
# canonical migration tool. It covers far more classes, Proguard rules, and
# Gradle dependencies. This file is a compact fallback for quick rewrites.
#
# Run from the repository root. Review the diff before committing.
#
# Usage:
#   bash .skills/media/migration/migrate-exoplayer-to-media3/references/rewrite-imports.sh [ROOT_DIR]
#
# Defaults to scanning ./app/src if no ROOT_DIR is provided.
#
# This script is intentionally conservative: it handles the common one-to-one
# package renames. Anything listed as "Removed" in references/package-mapping.md
# must be handled manually.

set -euo pipefail

ROOT_DIR="${1:-app/src}"

if [[ ! -d "$ROOT_DIR" ]]; then
  echo "error: directory not found: $ROOT_DIR" >&2
  exit 1
fi

# Pairs of: old_prefix -> new_prefix
# Order matters: more specific prefixes MUST come before more general ones.
MAPPINGS=(
  "com.google.android.exoplayer2.ui.StyledPlayerView=androidx.media3.ui.PlayerView"
  "com.google.android.exoplayer2.SimpleExoPlayer=androidx.media3.exoplayer.ExoPlayer"
  "com.google.android.exoplayer2.ExoPlayer=androidx.media3.exoplayer.ExoPlayer"
  "com.google.android.exoplayer2.source.hls=androidx.media3.exoplayer.hls"
  "com.google.android.exoplayer2.source.dash=androidx.media3.exoplayer.dash"
  "com.google.android.exoplayer2.source.smoothstreaming=androidx.media3.exoplayer.smoothstreaming"
  "com.google.android.exoplayer2.source.rtsp=androidx.media3.exoplayer.rtsp"
  "com.google.android.exoplayer2.source=androidx.media3.exoplayer.source"
  "com.google.android.exoplayer2.trackselection=androidx.media3.exoplayer.trackselection"
  "com.google.android.exoplayer2.drm=androidx.media3.exoplayer.drm"
  "com.google.android.exoplayer2.upstream.cache=androidx.media3.datasource.cache"
  "com.google.android.exoplayer2.upstream=androidx.media3.datasource"
  "com.google.android.exoplayer2.ui=androidx.media3.ui"
  "com.google.android.exoplayer2.ext.ima=androidx.media3.exoplayer.ima"
  "com.google.android.exoplayer2.ext.cast=androidx.media3.cast"
  # MediaSessionConnector has no direct Media3 equivalent. The package rewrite
  # below only fixes the import path; the code itself must be rebuilt on
  # androidx.media3.session.MediaSession / MediaSessionService.
  "com.google.android.exoplayer2.ext.mediasession=androidx.media3.session"
  # Catch-all: MUST stay last. Everything above is more specific and runs first.
  "com.google.android.exoplayer2=androidx.media3.common"
)

for pair in "${MAPPINGS[@]}"; do
  old="${pair%%=*}"
  new="${pair#*=}"
  # -print0 / xargs -0 to handle spaces in paths
  find "$ROOT_DIR" -type f \( -name "*.kt" -o -name "*.java" \) -print0 \
    | xargs -0 sed -i.bak "s#${old}#${new}#g"
done

# Clean up sed backups
find "$ROOT_DIR" -type f -name "*.bak" -delete

echo "Rewrite complete. Review 'git diff' and handle removed APIs manually:"
echo "  - MediaSessionConnector"
echo "  - PlayerNotificationManager"
echo "  - setCustomLayout -> setMediaButtonPreferences"
