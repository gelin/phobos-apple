package ru.gelin.android.phobosapple

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.info
import org.json.JSONArray
import java.util.concurrent.Future

class VideosRepository(
    private val context: Context
) {

    private val log = AnkoLogger(javaClass)

    /**
     * Loads videos in a background thread.
     */
    fun loadVideos(): Future<List<Video>> =
        doAsyncResult {
            loadCatalog()
        }

    private fun loadCatalog(): List<Video> {
        log.info("Loading catalog from resources")
        // TODO: update catalog from Internet
        val catalog = context.resources.openRawResource(R.raw.videos)

        val type = CatalogParser.VideoType.HEVC_FULLHD_SDR
        // TODO: choose another video type when possible
        log.info("Loading catalog type=$type")

        return CatalogParser(catalog).read(type).get()
    }

}


