#!/usr/bin/env bash
# Enforce the SKILL.md body size budget.
#
# - Soft target: 700 to 6,000 characters for compact skills.
# - Hard ceiling: 20,000 characters.
#
# The body is everything after the closing --- of the YAML frontmatter.
# Files exceeding the hard ceiling fail the job. Files under the soft floor
# produce a warning only. Files without a complete frontmatter block fail
# outright so a malformed file cannot slip past the ceiling with a 0 count.

set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

max=20000
min_target=700
max_target=6000

fail=0

while IFS= read -r file; do
    # Only the first two --- lines delimit frontmatter; later --- lines
    # (markdown horizontal rules) belong to the body.
    if ! awk 'NR==1 && /^---$/ {open=1; next} /^---$/ && open {closed=1; exit} END {exit !closed}' "$file"; then
        echo "::error file=$file::Missing or unclosed YAML frontmatter; cannot measure body"
        fail=1
        continue
    fi
    body=$(awk 'BEGIN{n=0} n<2 && /^---$/{n++; next} n>=2{print}' "$file")
    chars=$(printf '%s' "$body" | wc -m | tr -d ' ')
    printf '%s -> %s chars\n' "$file" "$chars"
    if [ "$chars" -gt "$max" ]; then
        echo "::error file=$file::Body exceeds hard ceiling of $max characters ($chars)"
        fail=1
    elif [ "$chars" -gt "$max_target" ]; then
        echo "::warning file=$file::Body exceeds soft target of $max_target characters ($chars). Consider moving content to references/."
    elif [ "$chars" -lt "$min_target" ]; then
        echo "::warning file=$file::Body is below soft target of $min_target characters ($chars). Consider expanding."
    fi
done < <(find .skills/media -name SKILL.md -type f | sort)

exit "$fail"
