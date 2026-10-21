<!-- This file is my (Zach) addition -->

# Local Development Guide

Quick reference for building, running, and testing tools locally on my machine.

NOTE: `// REF-agp9-build-fixes` marks changes I made to avoid kotlin build errors in my local setup.

---

## 0. Nix Development shell

Enter the project-specific development environment. This is required for all shell commands below.

```zsh
nix develop ./local-dev
```

---

## 1. Emulator Setup (one-time setup)

If you need to re-create the Light Phone III emulator profile from scratch:

### 1. Create the AVD

```zsh
avdmanager create avd \
  -n LightPhone3 \
  -k "system-images;android-34;default;x86_64" \
  --device "pixel" \
  --force
```

_Creates a fresh virtual device named `LightPhone3` based on the AOSP Android 14 (API 34) x86_64 system image._

### 2. Configure Light Phone III Display & Memory Limits (one-time setup)

Edit the `config.ini` to make the emulator more like a light phone.

```zsh
python3 - <<'PY'
from pathlib import Path

config = Path.home() / ".android/avd/LightPhone3.avd/config.ini"
settings = {
    "hw.lcd.width": "1080",
    "hw.lcd.height": "1240",
    "hw.lcd.density": "420",
    "hw.ramSize": "1536",
    "vm.heapSize": "256",
}

lines = config.read_text().splitlines()
lines = [
    line for line in lines
    if line.partition("=")[0].strip() not in settings
]
lines.extend(f"{key}={value}" for key, value in settings.items())
config.write_text("\n".join(lines) + "\n")
PY
```

_Sets the screen to 1080x1240 (~420 dpi) to match the Light Phone III form factor and caps memory to 1.5 GB RAM._

---

## 2. Running the Emulator

```zsh
./local-dev/start-emulator.sh
```

---

## 3. Building & Running Your Tool

Once the emulator is running and connected (verify with `adb devices`):

```zsh
./local-dev/build-install-launch.sh
```

---

## 4. LightOS System App Setup (Optional) <!-- I haven' tried this yet -->

To run the full LightOS interface as a privileged system app:

### 1. Generate Platform Signing Keys (One-time)

```zsh
mkdir -p sdk/emulator/keys
curl -s -o /tmp/platform.x509.pem https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.x509.pem
curl -s -o /tmp/platform.pk8 https://raw.githubusercontent.com/wfairclough/android_aosp_keys/refs/heads/master/platform.pk8

openssl pkcs8 -inform DER -nocrypt -in /tmp/platform.pk8 -out /tmp/platform.pem
openssl pkcs12 -export -in /tmp/platform.x509.pem -inkey /tmp/platform.pem -name platform -out /tmp/platform.p12 -passout pass:android
keytool -importkeystore -srckeystore /tmp/platform.p12 -srcstoretype PKCS12 -srcstorepass android -destkeystore sdk/emulator/keys/platform.jks -deststoretype JKS -deststorepass android
rm /tmp/platform.pk8 /tmp/platform.x509.pem /tmp/platform.pem /tmp/platform.p12
```

### 2. Build and push to `/system/priv-app`

```zsh
adb root
adb remount
./gradlew :sdk:emulator:assembleDebug
adb shell mkdir -p /system/priv-app/LightOSEmulator
adb push sdk/emulator/build/outputs/apk/debug/emulator-debug.apk /system/priv-app/LightOSEmulator/LightOSEmulator.apk
adb reboot
```

---

## 5. Useful Debugging Commands

- **View live logs filtered to your app**: <!-- Haven't confirmed this works -->
  ```zsh
  adb logcat -s LightTool
  ```
- **Uninstall the tool**:
  ```zsh
  adb uninstall com.thelightphone.app
  ```
- **Check connected devices**:
  ```zsh
  adb devices
  ```
