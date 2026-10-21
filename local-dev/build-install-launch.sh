#!/usr/bin/env bash

# Compile changes, install the updated APK, and force-restart the app on the
# running emulator.
#
# Steps:
#   ./gradlew :tool:installDebug
#     Compiles the :tool module and installs the new debug build onto the
#     active emulator.
#   adb shell am force-stop
#     Stops any currently running instance so the app restarts cleanly.
#   adb shell monkey
#     Launches the app using its registered launcher intent, without requiring
#     the activity class to be hardcoded.
#
# Requirements:
#   Run from the repository root with the emulator online.

set -Eeuo pipefail
trap 'echo "ERROR: Command failed at line $LINENO (exit code $?)" >&2' ERR

# Print informational messages in cyan.
info() {
  echo -e "\033[36m==> $*\033[0m"
}

info "Building and installing app..."
./gradlew :tool:installDebug

info "Checking connected devices..."
adb devices -l

info "Force-stopping app..."
adb shell am force-stop com.thelightphone.flights

info "Launching app..."
adb shell monkey -p com.thelightphone.flights 1

# NOTE: use -v for verbose output debugging
# adb shell monkey -v -p com.thelightphone.flights 1

info "Done."