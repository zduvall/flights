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

Tip for vscode. Open the project from inside the nix shell.

```zsh
nix develop ./local-dev -c code .
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

### 2. Configure Light Phone III Display & Memory Limits

Run the configuration script after creating the `LightPhone3` AVD. It updates the display dimensions and memory settings in the AVD's `config.ini`. These settings approximate the Light Phone III's display form factor and configure the emulator's memory parameters. The emulator may override the requested RAM allocation at runtime.

```zsh
./local-dev/configure-emulator.sh
```

Configured settings:

| Setting          | Purpose                  |
| ---------------- | ------------------------ |
| `hw.lcd.width`   | Display width in pixels  |
| `hw.lcd.height`  | Display height in pixels |
| `hw.lcd.density` | Display density in dpi   |
| `hw.ramSize`     | Requested RAM in MB      |
| `vm.heapSize`    | VM heap size in MB       |

Re-run this script whenever you recreate the AVD or want to reapply these settings.

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
