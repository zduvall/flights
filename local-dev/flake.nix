{
  description = "Light Phone SDK local development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-26.05-darwin";

  outputs = { nixpkgs, ... }:
    let
      # This environment currently targets your Intel Mac.
      system = "x86_64-darwin";

      androidPkgs = import nixpkgs {
        inherit system;

        config = {
          android_sdk.accept_license = true;

          # Allow only the unfree packages required by the Android SDK.
          allowUnfreePredicate = pkg:
            builtins.elem (nixpkgs.lib.getName pkg) [
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
      pkgs = androidPkgs;
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = [
          androidSdk
          pkgs.jdk17
          pkgs.openssl
        ];

        JAVA_HOME = "${pkgs.jdk17}";
        ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
        ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";

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
