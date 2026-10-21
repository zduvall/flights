#!/usr/bin/env bash

# Configure the LightPhone3 Android Virtual Device (AVD) to approximate the
# Light Phone III's display dimensions and set its memory limits.
#
# This script updates the AVD's config.ini in place:
#   - Sets the display resolution to 1080x1240 at 420 dpi.
#   - Sets the configured RAM size to 1536 MB and VM heap size to 256 MB.
#   - Replaces existing values for these keys instead of appending duplicates.
#   - Preserves all unrelated AVD settings.
#
# Run after creating the LightPhone3 AVD:
#   ./local-dev/configure-emulator.sh

set -euo pipefail

CONFIG="$HOME/.android/avd/LightPhone3.avd/config.ini"

if [[ ! -f "$CONFIG" ]]; then
  echo "ERROR: AVD configuration not found: $CONFIG" >&2
  echo "Create the LightPhone3 AVD first; see local-dev/README.md." >&2
  exit 1
fi

python3 - "$CONFIG" <<'PY'
from pathlib import Path
import sys

config = Path(sys.argv[1])
settings = {
    "hw.lcd.width": "1080",
    "hw.lcd.height": "1240",
    "hw.lcd.density": "420",
    "hw.ramSize": "1536",
    "vm.heapSize": "256",
}

lines = config.read_text().splitlines()
lines = [
    line
    for line in lines
    if line.partition("=")[0].strip() not in settings
]
lines.extend(f"{key}={value}" for key, value in settings.items())
config.write_text("\n".join(lines) + "\n")
PY

echo "Updated LightPhone3 AVD configuration:"
grep -E '^(hw\.lcd\.(width|height|density)|hw\.ramSize|vm\.heapSize)=' "$CONFIG"
