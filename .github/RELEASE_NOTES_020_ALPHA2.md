## MePIPE 0.2.0-alpha.2 — playback timeout diagnostics

Alpha.1 surfaced a generic `timeout` while requesting YouTube stream data. This build reports the last extractor endpoint (host and path), HTTP status/latency for completed requests, and the exception type for failed requests. The per-request deadline is now 45 seconds (read timeout 30 seconds); the overall playback-resolution limit remains 75 seconds.

This is diagnostic progress, **not a confirmed playback fix**. If the reported endpoint times out, the network/VPN may not be reaching that YouTube endpoint; share the exact on-screen diagnostic and whether search still works. No video IDs, request query parameters, OAuth tokens, or headers are included in the endpoint diagnostic.

### Install

Install `mepipe-v0.2.0-alpha.2-debug.apk`. If another MePIPE debug build is installed, uninstall it first because separate CI runs may use different debug signing keys. The `fdroid-unsigned` APK is not directly installable; F-Droid signs its own build.

CI verifies compilation and that the runtime dependency graph contains no Google Play Services, Firebase, or Play SDKs. It does not verify live YouTube playback or OAuth on a physical device.
