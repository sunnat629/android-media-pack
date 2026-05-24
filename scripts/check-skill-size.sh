#!/usr/bin/env bash
# Enforce the SKILL.md body size budget.
#
# - Soft target: 700 to 6,000 characters for compact skills.
# - Hard ceiling: 20,000 characters.
#
# The body is everything after the closing --- of the YAML frontmatter.
# Files exceeding the hard ceiling fail the job. Files under the soft floor
# produce a warning only.

set -euo pipefail

max=20000
min_target=700
max_target=6000

fail=0
shopt -s nullglob

for file in .skills/media/*/SKILL.md; do
    body=$(awk 'BEGIN{n=0} /^---$/{n++; next} n>=2{print}' "$file")
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
done

exit "$fail"
