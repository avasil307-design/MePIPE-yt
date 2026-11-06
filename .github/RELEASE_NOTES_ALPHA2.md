## MePIPE 0.1.0-alpha.2 — YouTube search and playback

The second alpha replaces the sample feed with real public YouTube search results and adds in-app video playback.

### What works

- Search public YouTube videos using NewPipe Extractor (no API key or YouTube account required).
- Load real result thumbnails, channel names, view counts, upload dates, and durations.
- Resolve progressive, HLS, or DASH streams and play them in the app with ExoPlayer.
- Search suggestions, loading states, retryable network/extraction errors, and the saved Material 3 color themes from alpha 1.

### Limits

- This is still an early alpha. Subscriptions, account login, offline downloads, background audio, playlists, and a personalized home feed are not implemented.
- YouTube may change its public endpoints; extraction can temporarily stop working. Availability depends on network and regional access.
- The APK is a debug build signed with the debug key, not a production release.

Download `mepipe-v0.1.0-alpha.2-debug.apk`. The adjacent `.sha256` file contains its checksum.
