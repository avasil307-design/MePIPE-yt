## MePIPE 0.2.0-alpha.1 — playback diagnostics, local recommendations, Google subscriptions preview

This is an early test build. It adds a 75-second deadline when resolving a YouTube stream, shorter per-request network timeouts, MP4-first stream selection, and separate messages for stream-resolution failures and ExoPlayer failures. These changes improve diagnostics; they do **not** prove that playback works on every network or device. Physical playback testing is still required.

The home screen now offers topic suggestions based on successful searches stored locally on this device. Очистить локальные темы можно в разделе **«Вы» → «Очистить историю рекомендаций»**. This is local topic personalization, not YouTube's personalized recommendation feed.

An experimental Google account screen uses the system browser, authorization code with PKCE, and Android Keystore encryption for the refresh token. It requests read-only YouTube subscriptions. A compatible public OAuth client and YouTube Data API v3 project must be configured by the tester; live OAuth and subscription loading have not yet been verified. Do not enter a Google password or client secret in MePIPE.

### Install

Install `mepipe-v0.2.0-alpha.1-debug.apk`. If another MePIPE debug build is installed, uninstall it first because separate CI runs may use different debug signing keys. The `fdroid-unsigned` APK is not directly installable; F-Droid signs its own build.

CI verifies compilation and that the runtime dependency graph contains no Google Play Services, Firebase, or Play SDKs. It does not verify live YouTube playback or Google OAuth.
