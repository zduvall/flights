{
  description = "Light Phone SDK local development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-26.05-darwin";

  outputs = { nixpkgs, ... }:
    let
      # Configured for an Intel Mac.
      system = "x86_64-darwin";

      # Create a separately configured nixpkgs instance for the Light Phone /
      # Android SDK so all Android-specific configuration can live in this file.
      androidPkgs = import nixpkgs {
        inherit system;

        config = {
          # Automatically accept the Android SDK license agreement.
          android_sdk.accept_license = true;

          # Allow only the unfree packages required by the Android SDK.
          allowUnfreePredicate = pkg:
            builtins.elem (nixpkgs.lib.getName pkg) [
              # For Light Phone / Android SDK
              "androidsdk"
              "android-sdk-build-tools"
              "android-sdk-cmdline-tools"
              "android-sdk-emulator"
              "android-sdk-platform-tools"
              "android-sdk-platforms"
              "android-sdk-system-image-34-default-x86_64"
              "android-sdk-system-image-36-default-x86_64"
              "android-sdk-tools"
              "build-tools"
              "cmake"
              "cmdline-tools"
              "emulator"
              "platform-tools"
              "platforms"
              "system-image-34-default-x86_64"
              "system-image-36-default-x86_64"
              "tools"
            ];
        };
      };

      # Use the built-in androidenv from my pinned 26.05 nixpkgs.
      androidComposition =
        androidPkgs.androidenv.composeAndroidPackages {
          includeEmulator = true;
          includeSystemImages = true;
          includeNDK = false;

          platformVersions = [ "34" "36" ];
          buildToolsVersions = [ "34.0.0" "35.0.0" "36.0.0" ];

          systemImageTypes = [ "default" ];
          abiVersions = [ "x86_64" ];
        };

      androidSdk = androidComposition.androidsdk;

      # Use the separately configured nixpkgs instance throughout the shell.
      pkgs = androidPkgs;
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        # 1. Add the custom Android SDK, Java 17, and OpenSSL to the environment.
        packages = [
          androidSdk

          # Java Development Kit (JDK) for compiling and running Java programs.
          # Note: the Light SDK targets Java 17 specifically (not the most up-to-date version).
          pkgs.jdk17

          # OpenSSL: required for AOSP platform keystore PKCS#12 conversion.
          pkgs.openssl
        ];

        # 2. Point JAVA_HOME, ANDROID_HOME, and ANDROID_SDK_ROOT to their Nix-managed paths.
        JAVA_HOME = "${pkgs.jdk17}";
        ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
        ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";

        # 3. Add Android SDK tools to PATH for terminal-based emulator development.
        # Allows for CLI commands like `emulator`, `adb`, `sdkmanager`, and `avdmanager`.
        shellHook = ''
          export PATH="${androidSdk}/libexec/android-sdk/emulator:$PATH"
          export PATH="${androidSdk}/libexec/android-sdk/platform-tools:$PATH"
          export PATH="${androidSdk}/libexec/android-sdk/cmdline-tools/latest/bin:$PATH"

          echo "Light Phone SDK development environment"
          echo "Java: $JAVA_HOME"
          echo "Android SDK: $ANDROID_HOME"
        '';
      };
    };
}
