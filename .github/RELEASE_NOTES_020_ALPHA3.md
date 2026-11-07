## MePIPE 0.2.0-alpha.3 — player controls, Russian audio preference, comments

This build adds explicit seek-back/forward, play/pause, playback-speed, video-quality, audio-track, and subtitle controls. When multiple quality/audio variants are available, MePIPE uses DASH so the player can expose track choices; the player prefers Russian audio and can switch tracks. If DASH playback fails, it retries once with the compatible muxed MP4 stream when available. Search requests use Russian localization and duplicate video IDs are removed from the result list.

The video page now loads YouTube comments, shows author/date/like count, supports loading another comments page, and reports disabled/unavailable comments separately.

These are new integrations that require testing against real YouTube videos. CI confirms compilation and the no-GMS dependency check, but does not test audio-track availability, comments, or player controls on Waydroid/Android. Likes, posting comments, subscription actions, and a full personalized YouTube home feed are not included in this alpha.

### Install

Install `mepipe-v0.2.0-alpha.3-debug.apk`. If another MePIPE debug build is installed, uninstall it first because separate CI runs may use different debug signing keys. The `fdroid-unsigned` APK is not directly installable; F-Droid signs its own build.
