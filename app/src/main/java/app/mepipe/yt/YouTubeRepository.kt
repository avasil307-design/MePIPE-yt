package app.mepipe.yt

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
import java.io.IOException
import java.util.concurrent.TimeUnit

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
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

    @Volatile
    private var initialized = false

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
        return info.relatedItems
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
    }

    @Throws(IOException::class, Exception::class)
    fun playbackSource(videoUrl: String): PlaybackSource {
        initialize()
        val info = StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
        val muxedVideo = info.videoStreams
            .filterNot { it.isVideoOnly }
            .maxByOrNull { stream ->
                stream.resolution.takeWhile(Char::isDigit).toIntOrNull() ?: 0
            }
        val muxedVideoUrl = muxedVideo?.url?.takeIf { it.isNotBlank() }
        if (muxedVideo != null && muxedVideoUrl != null) {
            return PlaybackSource(
                url = muxedVideoUrl,
                mimeType = muxedVideo.format?.mimeType,
            )
        }

        info.hlsUrl?.takeIf { it.isNotBlank() }?.let {
            return PlaybackSource(it, "application/x-mpegURL")
        }
        info.dashMpdUrl?.takeIf { it.isNotBlank() }?.let {
            return PlaybackSource(it, "application/dash+xml")
        }
        throw IOException("YouTube не вернул доступный поток для этого видео")
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

            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                return ExtractorResponse(
                    response.code,
                    response.message,
                    response.headers.toMultimap(),
                    responseText,
                    response.request.url.toString(),
                )
            }
        }
    }
}
