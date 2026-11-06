## MePIPE 0.1.0-alpha.2d — Android 10 search compatibility fix

Alpha.2c introduced a newer NewPipe Extractor, and Huawei Y8p reported a runtime `LinkageError` during search. A likely cause is a Java library API used by the extractor that is newer than Android 10's runtime API.

This build enables core library desugaring (`desugar_jdk_libs_nio` 2.1.5), matching the current NewPipe app's approach, so newer library APIs are available on supported Android versions. The app also shows the concrete `LinkageError` type and message if a compatibility issue remains. A physical Y8p retest is needed to confirm the diagnosis.

### Install

Download **`mepipe-v0.1.0-alpha.2d-debug.apk`**. If another MePIPE debug build is installed, uninstall it first because separate CI runs may use different debug signing keys. Do not install the `fdroid-unsigned` APK; F-Droid signs its own build.

This is an early alpha test build. CI verifies compilation and signing, but a physical Huawei Y8p test is still needed to confirm the runtime fix.
