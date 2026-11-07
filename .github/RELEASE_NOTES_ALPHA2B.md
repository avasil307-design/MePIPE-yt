## MePIPE 0.1.0-alpha.2b

This is a repackaged alpha.2 build with a new version code and clearly separated installable and F-Droid artifacts. App functionality is unchanged from alpha.2.

### Install on a phone

Download **`mepipe-v0.1.0-alpha.2b-debug.apk`**. It is signed with the Android debug certificate and can be installed for testing. If a previous MePIPE build signed with another debug certificate is already installed, uninstall it first; this CI build cannot update an APK signed with a different key.

### F-Droid/source build

F-Droid builds the app from source and signs its APK with the F-Droid key. An unsigned APK is not installable directly; do not use an unsigned build for manual installation.

The debug APK has an adjacent `.sha256` checksum file. This is an early alpha debug release, not a production build.
