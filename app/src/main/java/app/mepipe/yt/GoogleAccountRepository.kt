package app.mepipe.yt

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class YouTubeSubscription(
    val channelId: String,
    val title: String,
    val thumbnailUrl: String?,
)

/** Google OAuth via the system browser; no embedded login, GMS, or client secret. */
internal object GoogleAccountRepository {
    const val REDIRECT_URI = "app.mepipe.yt://oauth2redirect"
    private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
    private const val TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token"
    private const val SUBSCRIPTIONS_ENDPOINT = "https://youtube.googleapis.com/youtube/v3/subscriptions"
    private const val ACCOUNT_PREFS = "mepipe_google_account"
    private const val KEY_STATE = "oauth_state"
    private const val KEY_VERIFIER = "oauth_pkce_verifier"
    private const val KEY_REFRESH_TOKEN = "encrypted_refresh_token"
    private const val KEYSTORE_ALIAS = "mepipe_google_refresh_token"
    private const val OAUTH_SCOPE = "https://www.googleapis.com/auth/youtube.readonly"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    private val redirectState = MutableStateFlow<Uri?>(null)
    val redirects = redirectState.asStateFlow()

    fun publishRedirect(uri: Uri) {
        redirectState.value = uri
    }

    fun clearRedirect(uri: Uri) {
        if (redirectState.value == uri) redirectState.value = null
    }

    fun authorizationUri(context: Context, clientId: String): Uri {
        val cleanClientId = clientId.trim()
        require(cleanClientId.endsWith(".apps.googleusercontent.com")) {
            "Введите действующий OAuth Client ID из Google Cloud Console."
        }
        val state = randomUrlSafe(32)
        val verifier = randomUrlSafe(64)
        val challenge = Base64.encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        accountPreferences(context).edit()
            .putString(KEY_STATE, state)
            .putString(KEY_VERIFIER, verifier)
            .apply()
        return Uri.parse(AUTH_ENDPOINT).buildUpon()
            .appendQueryParameter("client_id", cleanClientId)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", OAUTH_SCOPE)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("access_type", "offline")
            .appendQueryParameter("prompt", "consent")
            .build()
    }

    suspend fun finishAuthorization(
        context: Context,
        clientId: String,
        redirect: Uri,
    ): List<YouTubeSubscription> = withContext(Dispatchers.IO) {
        redirect.getQueryParameter("error")?.let { error ->
            throw IOException("Вход Google отменён или отклонён: $error")
        }
        val preferences = accountPreferences(context)
        val expectedState = preferences.getString(KEY_STATE, null)
        val verifier = preferences.getString(KEY_VERIFIER, null)
        val actualState = redirect.getQueryParameter("state")
        if (expectedState.isNullOrBlank() || expectedState != actualState || verifier.isNullOrBlank()) {
            throw IOException("Не прошла проверка состояния OAuth. Начните вход ещё раз.")
        }
        val code = redirect.getQueryParameter("code")
            ?: throw IOException("Google не вернул код авторизации.")
        preferences.edit().remove(KEY_STATE).remove(KEY_VERIFIER).apply()

        val tokenJson = postForm(
            TOKEN_ENDPOINT,
            mapOf(
                "client_id" to clientId.trim(),
                "code" to code,
                "code_verifier" to verifier,
                "grant_type" to "authorization_code",
                "redirect_uri" to REDIRECT_URI,
            ),
        )
        val refreshToken = tokenJson.optString("refresh_token")
        if (refreshToken.isNotBlank()) saveRefreshToken(context, refreshToken)
        if (!hasRefreshToken(context)) {
            throw IOException("Google не выдал refresh token. Проверьте тип OAuth-клиента и повторите вход с подтверждением доступа.")
        }
        subscriptionsForRefreshToken(context, clientId.trim())
    }

    suspend fun refreshSubscriptions(context: Context, clientId: String): List<YouTubeSubscription> = withContext(Dispatchers.IO) {
        if (!hasRefreshToken(context)) throw IOException("Сначала подключите Google-аккаунт.")
        if (clientId.isBlank()) throw IOException("Не указан OAuth Client ID.")
        subscriptionsForRefreshToken(context, clientId.trim())
    }

    fun hasRefreshToken(context: Context): Boolean =
        accountPreferences(context).contains(KEY_REFRESH_TOKEN)

    fun disconnect(context: Context) {
        accountPreferences(context).edit()
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_STATE)
            .remove(KEY_VERIFIER)
            .apply()
        runCatching {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        }
    }

    private fun subscriptionsForRefreshToken(context: Context, clientId: String): List<YouTubeSubscription> {
        val refreshToken = loadRefreshToken(context)
        val accessToken = postForm(
            TOKEN_ENDPOINT,
            mapOf(
                "client_id" to clientId,
                "refresh_token" to refreshToken,
                "grant_type" to "refresh_token",
            ),
        ).optString("access_token").takeIf(String::isNotBlank)
            ?: throw IOException("Google не вернул access token.")

        val subscriptions = mutableListOf<YouTubeSubscription>()
        var pageToken: String? = null
        repeat(5) {
            val urlBuilder = Uri.parse(SUBSCRIPTIONS_ENDPOINT).buildUpon()
                .appendQueryParameter("part", "snippet")
                .appendQueryParameter("mine", "true")
                .appendQueryParameter("maxResults", "50")
            pageToken?.let { urlBuilder.appendQueryParameter("pageToken", it) }
            val request = Request.Builder()
                .url(urlBuilder.build().toString())
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/json")
                .build()
            val json = executeJson(request)
            val items = json.optJSONArray("items")
            if (items != null) {
                for (index in 0 until items.length()) {
                    val snippet = items.optJSONObject(index)?.optJSONObject("snippet") ?: continue
                    val channelId = snippet.optJSONObject("resourceId")?.optString("channelId").orEmpty()
                    val title = snippet.optString("title").trim()
                    if (channelId.isNotBlank() && title.isNotBlank()) {
                        val thumbnails = snippet.optJSONObject("thumbnails")
                        val thumbnail = thumbnails?.optJSONObject("default")?.optString("url")
                            ?.takeIf(String::isNotBlank)
                        subscriptions += YouTubeSubscription(channelId, title, thumbnail)
                    }
                }
            }
            pageToken = json.optString("nextPageToken").takeIf(String::isNotBlank) ?: return subscriptions
        }
        return subscriptions
    }

    private fun postForm(url: String, fields: Map<String, String>): JSONObject {
        val body = FormBody.Builder().apply { fields.forEach { (key, value) -> add(key, value) } }.build()
        val request = Request.Builder().url(url).post(body).header("Accept", "application/json").build()
        return executeJson(request)
    }

    private fun executeJson(request: Request): JSONObject {
        client.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    JSONObject(payload).optJSONObject("error")?.optString("message")
                        ?: JSONObject(payload).optString("error_description")
                }.getOrNull().orEmpty()
                throw IOException("HTTP ${response.code}${message.takeIf(String::isNotBlank)?.let { ": $it" }.orEmpty()}")
            }
            return runCatching { JSONObject(payload) }
                .getOrElse { throw IOException("Google вернул некорректный JSON.", it) }
        }
    }

    private fun accountPreferences(context: Context) =
        context.getSharedPreferences(ACCOUNT_PREFS, Context.MODE_PRIVATE)

    private fun randomUrlSafe(byteCount: Int): String {
        val bytes = ByteArray(byteCount).also(SecureRandom()::nextBytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun saveRefreshToken(context: Context, token: String) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = (keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey) ?: run {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generator.generateKey()
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val stored = cipher.iv + ciphertext
        accountPreferences(context).edit()
            .putString(KEY_REFRESH_TOKEN, Base64.encodeToString(stored, Base64.NO_WRAP))
            .apply()
    }

    private fun loadRefreshToken(context: Context): String {
        val stored = accountPreferences(context).getString(KEY_REFRESH_TOKEN, null)
            ?: throw IOException("Refresh token не найден. Подключите Google-аккаунт заново.")
        return try {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            if (bytes.size <= 12) throw IOException("Сохранённая Google-сессия повреждена.")
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
                ?: throw IOException("Ключ Google-сессии не найден.")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (exception: Exception) {
            throw IOException("Не удалось расшифровать Google-сессию. Подключите аккаунт заново.", exception)
        }
    }
}
