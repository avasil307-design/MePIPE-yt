# Google OAuth test setup

Google subscriptions in MePIPE are experimental. This flow has not yet been exercised with a real Google Cloud project or on a device. Do not treat a successful app build as proof that sign-in works.

## What the app does

- Opens the system browser; it does not collect or display your Google password.
- Uses OAuth authorization code with PKCE and requests only `https://www.googleapis.com/auth/youtube.readonly`.
- Exchanges the authorization code without embedding a client secret.
- Stores the returned refresh token encrypted with an Android Keystore AES-GCM key.
- Calls YouTube Data API v3 `subscriptions.list` for the signed-in user's subscriptions.

## Tester configuration

1. Create a Google Cloud project, enable **YouTube Data API v3**, and configure the OAuth consent screen. While the consent screen is in testing mode, add the testing Google account as a test user.
2. Create a **public/native OAuth client that supports authorization code + PKCE** and permits the exact redirect URI `app.mepipe.yt://oauth2redirect`.
3. In MePIPE, open **Вы → Аккаунт Google**, paste the public OAuth Client ID (ending in `.apps.googleusercontent.com`), and tap **Подключить Google**.

Google Cloud client types and redirect URI rules can vary. If the console does not permit this redirect URI for a public client, stop there: do not paste a web-client secret into the app and do not substitute an embedded WebView or a password form. The OAuth configuration then needs a provider-approved native redirect or a separately reviewed backend design.

Never send OAuth refresh/access tokens, Google passwords, or client secrets in chat or issue reports. The Client ID is public configuration, but OAuth consent and API quota still belong to the tester's Google Cloud project.

## F-Droid considerations

The app has no GMS dependency; authorization uses a browser and Android platform Keystore. F-Droid builds do not include a shared project secret or OAuth client ID. A tester must configure their own compatible public client. Whether Google permits a given client/redirect configuration for an F-Droid-signed package must be verified before claiming that account sign-in is supported in a catalog release.
