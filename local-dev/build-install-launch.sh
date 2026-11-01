#!/usr/bin/env bash

# Compile changes, install the updated APK, and launch the app
# on the official LightOS Emulator.

set -Eeuo pipefail
trap 'status=$?; echo "ERROR: Command failed at line $LINENO (exit code $status)" >&2' ERR

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ADB=(adb -e)
PACKAGE_NAME="com.thelightphone.flights"
EMULATOR_PACKAGE="com.thelightphone.sdk.emulator"
APK_DIR="$ROOT_DIR/tool/build/outputs/apk/debug"

info() {
  echo -e "\033[36m==> $*\033[0m"
}

info "Checking emulator..."
"${ADB[@]}" get-state >/dev/null

if ! "${ADB[@]}" shell pm path "$EMULATOR_PACKAGE" >/dev/null 2>&1; then
  echo "ERROR: LightOS Emulator is not installed." >&2
  echo "Run ./local-dev/setup-lightos-emulator.sh first." >&2
  exit 1
fi

info "Building Flights..."
./gradlew :tool:assembleDebug \
  -DlightSdk.allowAltServerPackage=true

shopt -s nullglob
APKS=("$APK_DIR"/*.apk)

if [[ "${#APKS[@]}" -ne 1 ]]; then
  echo "Expected exactly one debug APK; found ${#APKS[@]}." >&2
  exit 1
fi

info "Installing Flights..."
"${ADB[@]}" install -r "${APKS[0]}"

info "Force-stopping Flights..."
"${ADB[@]}" shell am force-stop "$PACKAGE_NAME"

info "Launching Flights..."
"${ADB[@]}" shell monkey -p "$PACKAGE_NAME" 1

info "Done."
