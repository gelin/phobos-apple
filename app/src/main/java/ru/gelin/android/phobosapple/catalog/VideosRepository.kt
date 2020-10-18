package ru.gelin.android.phobosapple.catalog

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.info
import ru.gelin.android.phobosapple.*
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

        // TODO: choose another video codec and resolution based on current device capabilities
        val codec = VideoCodec.H264
        val resolution = VideoResolution.FULLHD

        log.info("Loading catalog codec=$codec resolution=$resolution")

        return CatalogParser(catalog).read(codec, resolution).get()
    }

//    private fun getSupportedCodec(): VideoCodec

}


