# Подготовка MePIPE к F-Droid

MePIPE не использует Google Mobile Services, Firebase, Play Services, Google Analytics или Google Sign-In. В проекте есть ExoPlayer (`com.google.android.exoplayer`) — это библиотека воспроизведения с открытым исходным кодом, а не GMS.

Проверка зависимостей и сборка unsigned release APK:

```bash
./gradlew :app:verifyFdroidRuntimeDependencies :app:assembleRelease
```

F-Droid должен собрать приложение из исходного кода и подписать APK своим ключом. APK `app/build/outputs/apk/release/app-release-unsigned.apk`, собранный без подписи, сам по себе не предназначен для установки пользователями. Для ручной проверки скачивайте только подписанный [`debug APK из GitHub Release`](https://github.com/avasil307-design/MePIPE-yt/releases/download/v0.1.0-alpha.2d/mepipe-v0.1.0-alpha.2d-debug.apk); CI отдельно проверяет его сертификат и выдаёт отдельные артефакты для installable debug и unsigned F-Droid builds.

**Приложение пока не опубликовано в каталоге F-Droid.** Для публикации нужно отдельно отправить metadata и build recipe в [репозиторий fdroiddata](https://gitlab.com/fdroid/fdroiddata), после проверки F-Droid. MePIPE работает с YouTube — сервисом с ограничениями; перед подачей необходимо явно указать соответствующий Anti-Feature (`NonFreeNet`) и проверить текущие правила каталога.

Минимальная версия Android — 8.0 (API 26); Huawei Y8p на Android 10 по minSdk подходит. Если конкретная сборка всё ещё аварийно завершается на устройстве, нужен stack trace из AndroidRuntime/Logcat: совместимость нельзя достоверно подтвердить одной лишь сборкой на CI.
