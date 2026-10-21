#!/usr/bin/env bash

# Start the LightPhone3 emulator.
#
# Flags:
#   -avd LightPhone3       Specifies which virtual device to launch.
#   -writable-system       Enables write access to the /system partition
#                          (required if running the LightOS system app).
#   -no-snapshot-load      Avoids restoring potentially incompatible saved state.
#   -adb-path ...          Informs the emulator GUI where your adb binary is.
#   -dns-server 8.8.8.8,1.1.1.1
#                          Configures explicit public DNS servers (Google &
#                          Cloudflare); prevents DNS lookup failures on macOS
#                          where default host DNS bridging can fail.

set -euo pipefail

ADB_PATH="$(command -v adb)"

emulator \
  -avd LightPhone3 \
  -writable-system \
  -no-snapshot-load \
  -adb-path "$ADB_PATH" \
  -dns-server 8.8.8.8,1.1.1.1
