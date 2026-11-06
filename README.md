# MePIPE

An Android-first NewPipe-inspired client UI experiment using Jetpack Compose and Material You (Material 3).

## What is here

- Native Android app scaffold (Kotlin + Jetpack Compose)
- Four selectable Material 3 color palettes (Plant Life, Sage, Citrus, and Turquoise), each with coordinated light and dark variants; the choice is saved on-device
- Video-first home feed with wide thumbnails, compact channel metadata, category chips, and a Shorts shelf
- Familiar five-item bottom navigation, quick search, cast, notifications, and profile actions
- Russian-language sample feed with category filters and local search

The visual structure follows familiar mainstream video-app patterns rather than the more utility-focused layout common in Pipe clients, while keeping MePIPE branding distinct.

The app now searches public YouTube results through NewPipe Extractor, loads real thumbnails, resolves playable streams, and plays them with ExoPlayer. This is an independent lightweight client, not the upstream NewPipe app. Subscriptions, account login, offline downloads, and background playback are not included yet. Network access and YouTube availability can vary by region, and YouTube changes may temporarily break extraction.

## Build

Requirements: JDK 17 and Android SDK Platform 35.

```bash
./gradlew assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions also builds this APK on pushes and pull requests; download the `mepipe-debug-apk` artifact from the workflow run.

## License and upstream

This app uses [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) (GPL-3.0-or-later); see [`LICENSE`](LICENSE). NewPipe is an independent open-source Android project by NewPipe e.V. MePIPE is not affiliated with NewPipe e.V. or YouTube. The extractor parses public YouTube pages and endpoints; it is not an official YouTube API client.
