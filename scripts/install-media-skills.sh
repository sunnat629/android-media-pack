#!/usr/bin/env bash
# Install this repo's media skills into a target project's agent skills folder.

set -euo pipefail

target_project="${1:-.}"
repo_url="${2:-https://github.com/sunnat629/android-media-pack}"
ref="${3:-main}"
dest_subdir="${MEDIA_SKILLS_DEST:-.agents/skills}"
ref_type="${MEDIA_SKILLS_REF_TYPE:-heads}"
archive_url="${MEDIA_SKILLS_ARCHIVE_URL:-}"

repo_url="${repo_url%.git}"
repo_url="${repo_url%/}"

if [ -z "$archive_url" ]; then
    archive_url="$repo_url/archive/refs/$ref_type/$ref.tar.gz"
fi

target_project="$(cd "$target_project" && pwd)"
dest="$target_project/$dest_subdir"
tmp="$(mktemp -d)"

cleanup() {
    rm -rf "$tmp"
}
trap cleanup EXIT

curl -fsSL "$archive_url" | tar -xz -C "$tmp"

media_dir="$(find "$tmp" -type d -path '*/.skills/media' -print -quit)"
if [ -z "$media_dir" ]; then
    echo "No .skills/media folder found in archive: $archive_url" >&2
    exit 1
fi

mkdir -p "$dest"

find "$media_dir" -name SKILL.md -type f | while IFS= read -r skill_file; do
    skill_dir="$(dirname "$skill_file")"
    skill_name="$(basename "$skill_dir")"
    rm -rf "$dest/$skill_name"
    mkdir -p "$dest/$skill_name"
    cp -R "$skill_dir"/. "$dest/$skill_name"/
done

count="$(find "$dest" -mindepth 2 -maxdepth 2 -name SKILL.md | wc -l | tr -d ' ')"
echo "Installed $count media skills to $dest"
