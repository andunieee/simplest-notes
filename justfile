set shell := ["bash", "-euo", "pipefail", "-c"]

package := "com.andunie.notes"
apk := "app/build/outputs/apk/release/app-release.apk"

# List recipes
default:
    @just --list

# Build the release APK (signed with the debug key)
build:
    ./gradlew assembleRelease

# Build, then install on every connected device
install: build install-only

# Install the already-built APK on every connected device, one at a time
install-only:
    #!/usr/bin/env bash
    set -uo pipefail
    serials=$(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
    [ -n "$serials" ] || { echo "no devices connected" >&2; exit 1; }
    failed=0
    for s in $serials; do
        out=$(adb -s "$s" install -r "{{apk}}" 2>&1) && echo "✓ $s" || { echo "✗ $s: $out" >&2; failed=1; }
    done
    exit $failed

# Install and open the app on every connected device
run: install
    for s in $(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'); do \
        adb -s "$s" shell am start -n {{package}}/.MainActivity >/dev/null; \
    done

# Uninstall from every connected device
uninstall:
    for s in $(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'); do \
        adb -s "$s" uninstall {{package}} || true; \
    done

# Show connected devices
devices:
    adb devices -l
