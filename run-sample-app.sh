#!/usr/bin/env bash
# Build, install, and launch the Android sample app from the repo root.

set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$root_dir/sampleApp"
app_id="dev.sunnat629.androidmediaskill"
main_activity="$app_id/.MainActivity"

usage() {
    cat <<EOF
Usage: ./run-sample-app.sh [options]

Builds and installs sampleApp, then launches it on the connected device/emulator.

Options:
  --clean           Run Gradle clean before installing.
  --no-launch       Build and install only.
  --serial SERIAL   Target a specific adb device/emulator serial.
  -h, --help        Show this help.

Environment:
  GRADLE_BIN        Optional explicit Gradle executable path.
  ANDROID_SERIAL    Optional adb serial, same as --serial.
EOF
}

die() {
    echo "error: $*" >&2
    exit 1
}

clean=false
launch=true
serial="${ANDROID_SERIAL:-}"

while [ "$#" -gt 0 ]; do
    case "$1" in
        --clean)
            clean=true
            ;;
        --no-launch)
            launch=false
            ;;
        --serial)
            shift
            [ "$#" -gt 0 ] || die "--serial requires a value"
            serial="$1"
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            usage >&2
            die "unknown option: $1"
            ;;
    esac
    shift
done

[ -d "$project_dir" ] || die "sampleApp project not found at $project_dir"

is_android_sdk() {
    [ -n "$1" ] && [ -d "$1/platforms" ] && [ -d "$1/platform-tools" ]
}

find_android_sdk() {
    for candidate in \
        "${ANDROID_HOME:-}" \
        "${ANDROID_SDK_ROOT:-}" \
        "$HOME/Library/Android/sdk" \
        "$HOME/Android/Sdk" \
        "/opt/android-sdk"
    do
        if is_android_sdk "$candidate"; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
}

android_sdk="$(find_android_sdk)"
[ -n "$android_sdk" ] || die "Android SDK not found. Set ANDROID_HOME or install the SDK with Android Studio."
export ANDROID_HOME="$android_sdk"
export ANDROID_SDK_ROOT="$android_sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

command -v adb >/dev/null 2>&1 || die "adb not found on PATH. Install Android platform-tools or add adb to PATH."

find_cached_gradle() {
    [ -d "$HOME/.gradle/wrapper/dists" ] || return 0
    preferred_gradle="$(find "$HOME/.gradle/wrapper/dists" -path "*/gradle-9.4.1*/bin/gradle" -type f 2>/dev/null | sort | head -n 1)"
    if [ -n "$preferred_gradle" ]; then
        printf '%s\n' "$preferred_gradle"
        return 0
    fi

    latest_gradle="$(find "$HOME/.gradle/wrapper/dists" -path "*/gradle-9*/bin/gradle" -type f 2>/dev/null | sort | tail -n 1)"
    if [ -n "$latest_gradle" ]; then
        printf '%s\n' "$latest_gradle"
        return 0
    fi

    find "$HOME/.gradle/wrapper/dists" -path "*/gradle-8*/bin/gradle" -type f 2>/dev/null | sort | tail -n 1
}

if [ -n "${GRADLE_BIN:-}" ]; then
    [ -x "$GRADLE_BIN" ] || die "GRADLE_BIN is not executable: $GRADLE_BIN"
    gradle_cmd=("$GRADLE_BIN" -p "$project_dir")
elif [ -x "$project_dir/gradlew" ]; then
    gradle_cmd=("$project_dir/gradlew" -p "$project_dir")
elif [ -x "$root_dir/gradlew" ]; then
    gradle_cmd=("$root_dir/gradlew" -p "$project_dir")
elif command -v gradle >/dev/null 2>&1; then
    gradle_cmd=(gradle -p "$project_dir")
else
    cached_gradle="$(find_cached_gradle)"
    [ -n "$cached_gradle" ] || die "Gradle not found. Add a Gradle wrapper, install Gradle, or set GRADLE_BIN."
    gradle_cmd=("$cached_gradle" -p "$project_dir")
fi

adb_cmd=(adb)
if [ -n "$serial" ]; then
    export ANDROID_SERIAL="$serial"
    adb_cmd=(adb -s "$serial")
else
    device_serials=()
    while IFS= read -r device_serial; do
        device_serials+=("$device_serial")
    done < <(
        adb devices | sed -n '/[[:space:]]device[[:space:]]*$/ {
            s/[[:space:]]device[[:space:]]*$//
            p
        }'
    )

    if [ "${#device_serials[@]}" -eq 0 ]; then
        die "no connected Android device/emulator found. Start one, then retry."
    fi

    if [ "${#device_serials[@]}" -eq 1 ]; then
        serial="${device_serials[0]}"
    else
        for device_serial in "${device_serials[@]}"; do
            case "$device_serial" in
                adb-*) ;;
                *)
                    serial="$device_serial"
                    break
                    ;;
            esac
        done

        if [ -z "$serial" ]; then
            echo "Multiple adb devices found:" >&2
            printf '  %s\n' "${device_serials[@]}" >&2
            die "choose one with --serial SERIAL"
        fi

        echo "Multiple adb devices found; using $serial. Pass --serial SERIAL to choose another."
    fi

    export ANDROID_SERIAL="$serial"
    adb_cmd=(adb -s "$serial")
fi

"${adb_cmd[@]}" get-state >/dev/null 2>&1 || die "adb device is not ready: $serial"

tasks=()
if [ "$clean" = true ]; then
    tasks+=(clean)
fi
tasks+=(:app:installDebug)

echo "Using Gradle: ${gradle_cmd[*]}"
echo "Building and installing sampleApp..."
"${gradle_cmd[@]}" "${tasks[@]}"

if [ "$launch" = true ]; then
    echo "Launching $main_activity..."
    "${adb_cmd[@]}" shell am start -n "$main_activity" >/dev/null
fi

echo "Done."
