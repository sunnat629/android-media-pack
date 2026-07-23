#!/usr/bin/env bash
# Validate YAML frontmatter in every SKILL.md under .skills/media.
#
# Required fields (per CONTRIBUTING.md):
#   name, description, license: Apache-2.0,
#   metadata.author, metadata.version, metadata.target_media3_version,
#   metadata.last_reviewed, metadata.keywords
#
# Also checks:
#   - frontmatter block is present and properly closed
#   - name matches the containing folder name
#   - target_media3_version matches the pinned version in MEDIA3_VERSION

set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

pinned_media3="$(tr -d '[:space:]' < MEDIA3_VERSION)"
fail=0

err() {
    echo "::error file=$1::$2"
    fail=1
}

while IFS= read -r file; do
    # Frontmatter must open on line 1 and close before EOF.
    if [ "$(head -n 1 "$file")" != "---" ]; then
        err "$file" "Missing YAML frontmatter opening --- on line 1"
        continue
    fi
    fm=$(awk 'NR==1 && /^---$/ {next} /^---$/ {exit} {print}' "$file")
    if ! awk 'NR==1 && /^---$/ {open=1; next} /^---$/ && open {closed=1; exit} END {exit !closed}' "$file"; then
        err "$file" "Frontmatter is never closed with ---"
        continue
    fi

    for field in 'name:' 'description:' 'license: Apache-2.0' 'author:' 'version:' 'target_media3_version:' 'last_reviewed:' 'keywords:'; do
        if ! printf '%s\n' "$fm" | grep -q "^[[:space:]]*$field"; then
            err "$file" "Frontmatter missing required field: $field"
        fi
    done

    folder=$(basename "$(dirname "$file")")
    fm_name=$(printf '%s\n' "$fm" | awk '/^name:/ {print $2; exit}')
    if [ "$fm_name" != "$folder" ]; then
        err "$file" "Frontmatter name '$fm_name' does not match folder '$folder'"
    fi

    fm_media3=$(printf '%s\n' "$fm" | awk -F'"' '/target_media3_version:/ {print $2; exit}')
    if [ "$fm_media3" != "$pinned_media3" ]; then
        err "$file" "target_media3_version '$fm_media3' does not match pinned MEDIA3_VERSION '$pinned_media3'"
    fi
done < <(find .skills/media -name SKILL.md -type f | sort)

if [ "$fail" -eq 0 ]; then
    echo "All SKILL.md frontmatter valid."
fi
exit "$fail"
