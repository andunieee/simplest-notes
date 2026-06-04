set shell := ["bash", "-euo", "pipefail", "-c"]

# Build the release APK and install it on every connected device
android:
    #!/usr/bin/env bash
    set -euo pipefail
    ./gradlew assembleRelease
    apk="app/build/outputs/apk/release/app-release.apk"
    serials=$(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
    [ -n "$serials" ] || { echo "no devices connected" >&2; exit 1; }
    failed=0
    for s in $serials; do
        out=$(adb -s "$s" install -r "$apk" 2>&1) && echo "✓ $s" || { echo "✗ $s: $out" >&2; failed=1; }
    done
    exit "$failed"
