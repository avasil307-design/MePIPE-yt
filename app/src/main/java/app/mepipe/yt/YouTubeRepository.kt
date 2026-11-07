package app.mepipe.yt

import android.os.SystemClock
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as ExtractorRequest
import org.schabi.newpipe.extractor.downloader.Response as ExtractorResponse
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

internal data class VideoResult(
    val title: String,
    val channel: String,
    val metadata: String,
    val duration: String,
    val thumbnailUrl: String?,
    val url: String,
)

internal data class PlaybackSource(
    val url: String,
    val mimeType: String? = null,
)

internal object YouTubeRepository {
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:153.0) " +
            "Gecko/20100101 Firefox/153.0"

    @Volatile
    private var initialized = false

    private val playbackSequence = AtomicLong(0)
    private val activePlaybackAttempt = AtomicLong(0)
    private val playbackAttemptOnThread = ThreadLocal<Long?>()
    private val playbackProgress = MutableStateFlow("Подготовка к запросу видеопотока")
    val playbackDiagnostics = playbackProgress.asStateFlow()

    fun beginPlaybackAttempt(): Long {
        val attempt = playbackSequence.incrementAndGet()
        activePlaybackAttempt.set(attempt)
        publishPlaybackProgress(attempt, "Подключаюсь к YouTube за ссылкой на поток…")
        return attempt
    }

    private fun publishPlaybackProgress(attempt: Long, message: String) {
        if (activePlaybackAttempt.get() == attempt) playbackProgress.value = message
    }

    private fun endpointLabel(url: okhttp3.HttpUrl): String = "${url.host}${url.encodedPath}"

    @Synchronized
    private fun initialize() {
        if (!initialized) {
            NewPipe.init(ExtractorDownloader())
            initialized = true
        }
    }

    @Throws(IOException::class, ReCaptchaException::class)
    fun search(query: String): List<VideoResult> {
        initialize()
        val service = ServiceList.YouTube
        val queryHandler = service.searchQHFactory.fromQuery(query)
        val info = SearchInfo.getInfo(service, queryHandler)
        val videos = info.relatedItems
            .filterIsInstance<StreamInfoItem>()
            .map { item ->
                val thumbnail = item.thumbnails.maxByOrNull { it.width }?.url
                val views = item.viewCount.takeIf { it >= 0 }?.let(::formatViews)
                val date = item.textualUploadDate?.takeIf { it.isNotBlank() }
                val details = listOfNotNull(views, date).joinToString(" · ")
                    .ifBlank { "YouTube" }
                VideoResult(
                    title = item.name,
                    channel = item.uploaderName.ifBlank { "YouTube" },
                    metadata = details,
                    duration = formatDuration(item.duration),
                    thumbnailUrl = thumbnail,
                    url = item.url,
                )
            }
        if (videos.isEmpty()) {
            Log.w("MePIPE", "YouTube search response contained no video items")
        }
        return videos
    }

    @Throws(IOException::class, Exception::class)
    fun playbackSource(videoUrl: String, attempt: Long): PlaybackSource {
        val previousAttempt = playbackAttemptOnThread.get()
        playbackAttemptOnThread.set(attempt)
        try {
            initialize()
            publishPlaybackProgress(attempt, "Запрашиваю данные видео у YouTube…")
            val info = try {
                StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
            } catch (exception: Exception) {
                publishPlaybackProgress(
                    attempt,
                    "Извлечение остановилось: ${exception.javaClass.simpleName}: ${exception.message.orEmpty().take(120)}",
                )
                throw exception
            }
            publishPlaybackProgress(attempt, "Ссылка получена; выбираю совместимый формат…")
            val muxedStreams = info.videoStreams.filterNot { it.isVideoOnly }
            val mp4Streams = muxedStreams.filter { it.format?.mimeType == "video/mp4" }
            val muxedVideo = (mp4Streams.ifEmpty { muxedStreams }).maxByOrNull { stream ->
                stream.resolution.takeWhile(Char::isDigit).toIntOrNull() ?: 0
            }
            val muxedVideoUrl = muxedVideo?.url?.takeIf { it.isNotBlank() }
            if (muxedVideo != null && muxedVideoUrl != null) {
                publishPlaybackProgress(attempt, "Поток найден (${muxedVideo.resolution}, ${muxedVideo.format?.mimeType ?: "неизвестный формат"}).")
                return PlaybackSource(
                    url = muxedVideoUrl,
                    mimeType = muxedVideo.format?.mimeType,
                )
            }

            info.hlsUrl?.takeIf { it.isNotBlank() }?.let {
                publishPlaybackProgress(attempt, "Найден HLS-поток.")
                return PlaybackSource(it, "application/x-mpegURL")
            }
            info.dashMpdUrl?.takeIf { it.isNotBlank() }?.let {
                publishPlaybackProgress(attempt, "Найден DASH-поток.")
                return PlaybackSource(it, "application/dash+xml")
            }
            throw IOException("YouTube вернул страницу, но доступный поток не найден.")
        } finally {
            playbackAttemptOnThread.set(previousAttempt)
        }
    }

    private fun formatViews(count: Long): String = when {
        count >= 1_000_000 -> "${count / 1_000_000} млн просмотров"
        count >= 10_000 -> "${count / 1_000} тыс. просмотров"
        else -> "$count просмотров"
    }

    private fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "Прямой эфир"
        val hours = seconds / 3_600
        val minutes = (seconds % 3_600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, remainingSeconds)
        } else {
            "%d:%02d".format(minutes, remainingSeconds)
        }
    }

    private class ExtractorDownloader : Downloader() {
        private val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()

        override fun execute(request: ExtractorRequest): ExtractorResponse {
            val body = request.dataToSend()?.toRequestBody()
            val requestBuilder = okhttp3.Request.Builder()
                .url(request.url())
                .method(request.httpMethod(), body)
                .header("User-Agent", USER_AGENT)

            request.headers().forEach { (name, values) ->
                requestBuilder.removeHeader(name)
                values.forEach { value -> requestBuilder.addHeader(name, value) }
            }

            val httpRequest = requestBuilder.build()
            val playbackAttempt = playbackAttemptOnThread.get()
            val endpoint = endpointLabel(httpRequest.url)
            val startedAt = SystemClock.elapsedRealtime()
            if (playbackAttempt != null) {
                publishPlaybackProgress(playbackAttempt, "Запрос к $endpoint…")
            }
            try {
                client.newCall(httpRequest).execute().use { response ->
                    val responseText = response.body?.string().orEmpty()
                    val elapsedMs = SystemClock.elapsedRealtime() - startedAt
                    if (playbackAttempt != null) {
                        val status = "Ответ $endpoint: HTTP ${response.code} за ${elapsedMs} мс."
                        publishPlaybackProgress(playbackAttempt, status)
                        Log.i("MePIPE", status)
                    }
                    return ExtractorResponse(
                        response.code,
                        response.message,
                        response.headers.toMultimap(),
                        responseText,
                        response.request.url.toString(),
                    )
                }
            } catch (exception: IOException) {
                val elapsedMs = SystemClock.elapsedRealtime() - startedAt
                val timedOut = exception.javaClass.simpleName.contains("timeout", ignoreCase = true)
                    || exception.message?.contains("timeout", ignoreCase = true) == true
                val detail = "${if (timedOut) "Тайм-аут" else "Ошибка HTTP"} на $endpoint за ${elapsedMs} мс: " +
                    (exception.message ?: exception.javaClass.simpleName)
                if (playbackAttempt != null) publishPlaybackProgress(playbackAttempt, detail)
                Log.e("MePIPE", detail, exception)
                throw IOException(detail, exception)
            }
        }
    }
}
