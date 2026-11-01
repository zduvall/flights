<!-- This file is my (Zach) addition -->

# Local Development Guide

Quick reference for building, running, and testing tools locally on my machine.

NOTE: `// REF-agp9-build-fixes` marks changes I made to avoid kotlin build errors in local setup. These errors also exist on `main` -- tracked in [issue 310](https://github.com/lightphone/light-sdk/issues/310)

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

_Creates a fresh virtual device named `LightPhone3` based on the AOSP Android 14 (API 34) `x86_64` system image._

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

## 3. LightOS Emulator Setup

Install official LightOS Emulator on the `LightPhone3` AVD -- provides a closer simulation of actual Light Phone III. Run after starting the emulator:

```zsh
./local-dev/setup-lightos-emulator.sh
```

_Pressing the Android Home button (`adb -e shell input keyevent KEYCODE_HOME`) will return to the LightOS interface rather than the default Android launcher._

## 4. Building & Running the Tool

### 1. Configure the LightOS Emulator server

In `tool/lighttool.toml`, make sure `serverPackage` in the existing `[tool]` section:

```toml
serverPackage = "com.thelightphone.sdk.emulator"
```

### 2. Build, install, and launch the tool

Once the emulator is running and LightOS Emulator is installed, run this script to test code changes.

```zsh
./local-dev/build-install-launch.sh
```

---

## 5. Before Submitting

Before submitting Flights through the Light Phone Developer Dashboard, restore the production LightOS server package in `tool/lighttool.toml`:

```toml
serverPackage = "com.lightos"
```

Increment the `versionCode`, update the `versionName` in `tool/lighttool.toml`, and then tag the release in Git.

```zsh
git tag flights-0.1.0 && git push origin flights-0.1.0
```

Use the git tag when submitting the Git Ref / SHA for my release.
