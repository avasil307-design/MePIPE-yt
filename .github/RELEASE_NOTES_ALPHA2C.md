## MePIPE 0.1.0-alpha.2c — search compatibility update

This update keeps alpha.2b's features and refreshes the YouTube search stack:

- Pin NewPipe Extractor to upstream commit `6cb36e982311dc1c529997bc329013afe75f37d1`, also used by the current NewPipe app source.
- Update the extractor User-Agent to Firefox 153 ESR.
- Log extractor failures and explain that an empty response can happen when YouTube limits a shared VPN IP. Try another VPN endpoint or retry later; VPN does not guarantee access.

### Install on a phone

Download **`mepipe-v0.1.0-alpha.2c-debug.apk`**. It is signed with the Android debug certificate. If another MePIPE debug build is already installed, uninstall it first because separate CI builds can have different debug keys.

### F-Droid/source build

F-Droid builds the app from source and signs its APK with the F-Droid key. Unsigned APKs cannot be installed directly. Both release APK artifacts have adjacent `.sha256` checksum files.

This is an early alpha debug release, not a production build. YouTube may still block or throttle VPN exit IPs; switching VPN servers or temporarily disabling the VPN can help distinguish an IP restriction from a parser issue.
