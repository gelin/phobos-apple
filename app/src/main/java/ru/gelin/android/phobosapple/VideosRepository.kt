package ru.gelin.android.phobosapple

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.info
import org.json.JSONArray
import java.net.URL
import java.util.concurrent.Future

class VideosRepository {

    private val log = AnkoLogger(javaClass)

    private val catalogUrl = URL("http://a1.phobos.apple.com/us/r1000/000/Features/atv/AutumnResources/videos/entries.json")

    /**
     * Loads videos in a background thread.
     */
    fun loadVideos(): Future<List<Video>> =
        doAsyncResult {
            loadCatalogue()
        }


    private fun loadCatalogue(): List<Video> {
        val catalog = catalogUrl.openStream().reader().readText()
        val assets = JSONArray(catalog)
        val result = mutableListOf<Video>()
        for (i in 0 until assets.length()) {
            val assetsObject = assets.getJSONObject(i)
            val assetsList = assetsObject.getJSONArray("assets")
            for (j in 0 until assetsList.length()) {
                val asset = assetsList.getJSONObject(j)
                if (asset.getString("type") == "video") {
                    val movie = Video(
                        url = asset.getString("url"),
                        location = asset.getString("accessibilityLabel"),
                        timeOfDay = asset.getString("timeOfDay")
                    )
                    result.add(movie)
                }
            }
        }
        result.shuffle()
        log.info { "Loaded ${result.size} videos" }
        return result
    }

}

data class Video(
    val url: String,
    val location: String,
    val timeOfDay: String
)
