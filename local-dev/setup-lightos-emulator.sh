
#!/usr/bin/env bash

# Build and install the official LightOS Emulator as a privileged
# system app on the LightPhone3 AVD.
#
# Run after starting the emulator with -writable-system:
#   ./local-dev/setup-lightos-emulator.sh

set -Eeuo pipefail
trap 'status=$?; echo "ERROR: Failed at line $LINENO (exit code $status)" >&2' ERR

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ADB=(adb -e)

EMULATOR_PACKAGE="com.thelightphone.sdk.emulator"
KEYSTORE="sdk/emulator/keys/platform.jks"
APK="sdk/emulator/build/outputs/apk/debug/emulator-debug.apk"

# Wait until Android has completed booting.
wait_for_boot() {
  for ((i = 0; i < 90; i++)); do
    if [[ "$("${ADB[@]}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
      return 0
    fi
    sleep 1
  done

  echo "ERROR: Emulator did not finish booting." >&2
  exit 1
}

echo "==> Checking emulator..."

"${ADB[@]}" get-state >/dev/null
wait_for_boot

# The platform test key only works with test-key system images.
BUILD_DESCRIPTION="$("${ADB[@]}" shell getprop ro.build.description | tr -d '\r')"

if [[ "$BUILD_DESCRIPTION" != *test-keys ]]; then
  echo "ERROR: Emulator must use a test-keys system image." >&2
  echo "Build description: $BUILD_DESCRIPTION" >&2
  exit 1
fi

# Enable root access and writable system partition.
echo "==> Preparing system partition..."

"${ADB[@]}" root
"${ADB[@]}" wait-for-device
wait_for_boot

if ! REMOUNT_OUTPUT="$("${ADB[@]}" remount 2>&1)"; then
  echo "$REMOUNT_OUTPUT"

  if [[ "$REMOUNT_OUTPUT" != *verity* ]]; then
    echo "ERROR: Could not remount system partition." >&2
    exit 1
  fi

  echo "==> Disabling verified boot..."

  "${ADB[@]}" disable-verity
  "${ADB[@]}" reboot
  "${ADB[@]}" wait-for-device
  wait_for_boot

  "${ADB[@]}" root
  "${ADB[@]}" wait-for-device
  wait_for_boot
  "${ADB[@]}" remount
else
  echo "$REMOUNT_OUTPUT"
fi

echo "==> Rebooting to activate writable system partition..."

"${ADB[@]}" reboot
"${ADB[@]}" wait-for-device
wait_for_boot

# Restore root ADB access and activate the OverlayFS mount.
"${ADB[@]}" root
"${ADB[@]}" wait-for-device
wait_for_boot

echo "==> Mounting writable system partition..."
"${ADB[@]}" remount

# Verify that /system is actually writable.
echo "==> Verifying writable system partition..."

"${ADB[@]}" shell \
  'touch /system/.lightos-write-test && rm /system/.lightos-write-test'

# Generate the AOSP platform signing keystore if necessary.
if [[ ! -f "$KEYSTORE" ]]; then
  echo "==> Generating platform signing key..."

  TMP_DIR="$(mktemp -d)"
  trap 'rm -rf "$TMP_DIR"' EXIT

  curl -fsSL \
    https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.x509.pem \
    -o "$TMP_DIR/platform.x509.pem"

  curl -fsSL \
    https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.pk8 \
    -o "$TMP_DIR/platform.pk8"

  openssl pkcs8 \
    -inform DER \
    -nocrypt \
    -in "$TMP_DIR/platform.pk8" \
    -out "$TMP_DIR/platform.pem"

  openssl pkcs12 -export \
    -in "$TMP_DIR/platform.x509.pem" \
    -inkey "$TMP_DIR/platform.pem" \
    -name platform \
    -out "$TMP_DIR/platform.p12" \
    -passout pass:android

  mkdir -p "$(dirname "$KEYSTORE")"

  "$JAVA_HOME/bin/keytool" -importkeystore \
    -srckeystore "$TMP_DIR/platform.p12" \
    -srcstoretype PKCS12 \
    -srcstorepass android \
    -destkeystore "$KEYSTORE" \
    -deststoretype JKS \
    -deststorepass android \
    -noprompt

  rm -rf "$TMP_DIR"
  trap 'status=$?; echo "ERROR: Failed at line $LINENO (exit code $status)" >&2' ERR
fi

# Build LightOS Emulator with platform signing.
echo "==> Building LightOS Emulator..."

./gradlew :sdk:emulator:assembleDebug --console=plain

if [[ ! -f "$APK" ]]; then
  echo "ERROR: Emulator APK not found: $APK" >&2
  exit 1
fi

# Install as a privileged system application.
echo "==> Installing LightOS Emulator..."

"${ADB[@]}" shell mkdir -p /system/priv-app/LightOSEmulator

"${ADB[@]}" push "$APK" \
  /system/priv-app/LightOSEmulator/LightOSEmulator.apk

echo "==> Rebooting emulator..."

"${ADB[@]}" reboot
"${ADB[@]}" wait-for-device
wait_for_boot

# Verify installation and system UID.
echo "==> Verifying LightOS Emulator..."

"${ADB[@]}" shell pm path "$EMULATOR_PACKAGE"

UID_INFO="$("${ADB[@]}" shell dumpsys package "$EMULATOR_PACKAGE" |
  grep -m 1 -E 'userId=1000|appId=1000|uid=1000' || true)"

if [[ -z "$UID_INFO" ]]; then
  echo "ERROR: LightOS Emulator is not running with UID 1000." >&2
  exit 1
fi

echo "$UID_INFO"
echo "==> LightOS Emulator successfully installed!"

# Configure LightOS Emulator as the default launcher.
echo "==> Setting LightOS Emulator as default launcher..."

"${ADB[@]}" shell cmd package set-home-activity \
  "$EMULATOR_PACKAGE/.MainActivity"

# Disable Android animations to better match the physical Light Phone.
echo "==> Disabling Android animations..."

"${ADB[@]}" shell settings put global window_animation_scale 0
"${ADB[@]}" shell settings put global transition_animation_scale 0
"${ADB[@]}" shell settings put global animator_duration_scale 0

echo "==> LightOS Emulator successfully installed and configured!"