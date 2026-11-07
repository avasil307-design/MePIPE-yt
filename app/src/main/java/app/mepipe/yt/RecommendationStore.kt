package app.mepipe.yt

import android.content.Context

/** Local-only topic personalization; no search history is sent to a third party. */
internal object RecommendationStore {
    private const val PREFS = "mepipe_settings"
    private const val KEY_RECENT_QUERIES = "recent_recommendation_queries"
    private const val MAX_RECENT_QUERIES = 6

    private val starterTopics = listOf("музыка", "технологии", "новости", "игры", "наука")

    fun topics(context: Context): List<String> {
        val recent = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_RECENT_QUERIES, "")
            .orEmpty()
            .split('\n')
            .map(String::trim)
            .filter(String::isNotBlank)
        return (recent + starterTopics + "популярное").distinct().take(8)
    }

    fun recordSuccessfulSearch(context: Context, query: String) {
        val cleanQuery = query.trim().take(80)
        if (cleanQuery.isBlank()) return
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = preferences.getString(KEY_RECENT_QUERIES, "")
            .orEmpty()
            .split('\n')
            .map(String::trim)
            .filter(String::isNotBlank)
        val updated = (listOf(cleanQuery) + previous.filterNot { it.equals(cleanQuery, ignoreCase = true) })
            .take(MAX_RECENT_QUERIES)
        preferences.edit().putString(KEY_RECENT_QUERIES, updated.joinToString("\n")).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_RECENT_QUERIES)
            .apply()
    }
}
