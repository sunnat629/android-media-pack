#!/usr/bin/env bash
# Install this repo's media skills into a target project's agent skills folder.
#
# Thin wrapper for one-line curl installs. It downloads the repo archive once,
# then delegates to the bundled bin/android-media-skill CLI so the install
# logic lives in exactly one place.

set -euo pipefail

target_project="${1:-.}"
repo_url="${2:-https://github.com/sunnat629/android-media-pack}"
ref="${3:-main}"
ref_type="${MEDIA_SKILLS_REF_TYPE:-heads}"
archive_url="${MEDIA_SKILLS_ARCHIVE_URL:-}"

repo_url="${repo_url%/}"
repo_url="${repo_url%.git}"

if [ -z "$archive_url" ]; then
    archive_url="$repo_url/archive/refs/$ref_type/$ref.tar.gz"
fi

tmp="$(mktemp -d)"
cleanup() {
    rm -rf "$tmp"
}
trap cleanup EXIT

curl -fsSL "$archive_url" -o "$tmp/pack.tar.gz"
tar -xzf "$tmp/pack.tar.gz" -C "$tmp"

cli="$(find "$tmp" -type f -path '*/bin/android-media-skill' -print -quit)"
if [ -z "$cli" ]; then
    echo "No bin/android-media-skill found in archive: $archive_url" >&2
    exit 1
fi

MEDIA_SKILLS_ARCHIVE_URL="file://$tmp/pack.tar.gz" \
    bash "$cli" install "$target_project" "$repo_url" "$ref"
