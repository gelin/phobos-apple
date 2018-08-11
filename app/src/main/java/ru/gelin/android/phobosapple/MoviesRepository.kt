package ru.gelin.android.phobosapple

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import org.json.JSONArray
import java.net.URL

class MoviesRepository {

    private val log = AnkoLogger(javaClass)

    private val catalogUrl = URL("http://a1.phobos.apple.com/us/r1000/000/Features/atv/AutumnResources/videos/entries.json")

    private var movies = listOf<Movie>()
    private var index = 0

    init {
        async(UI) {
            val loadingMovies: Deferred<List<Movie>> = bg {
                    loadMovies()
            }
            try {
                val loadedMovies = loadingMovies.await()
                log.info { "Loaded ${loadedMovies.size} movies" }
                movies = loadedMovies
            } catch (e: Exception) {
                log.warn("Failed to load movies", e)
            }
        }
    }

    fun nextMovie(): Movie? {
        if (index >= movies.size) {
            index = 0
        }
        val movie = movies.getOrNull(index)
        index++
        return movie
    }

    private fun loadMovies(): List<Movie> {
        val catalog = catalogUrl.openStream().reader().readText()
        val assets = JSONArray(catalog)
        val result = mutableListOf<Movie>()
        for (i in 0 until assets.length()) {
            val assetsObject = assets.getJSONObject(i)
            val assetsList = assetsObject.getJSONArray("assets")
            for (j in 0 until assetsList.length()) {
                val asset = assetsList.getJSONObject(j)
                if (asset.getString("type") == "video") {
                    val movie = Movie(
                        url = asset.getString("url"),
                        location = asset.getString("accessibilityLabel"),
                        timeOfDay = asset.getString("timeOfDay")
                    )
                    result.add(movie)
                }
            }
        }
        result.shuffle()
        return result
    }

}

data class Movie(
    val url: String,
    val location: String,
    val timeOfDay: String
)
