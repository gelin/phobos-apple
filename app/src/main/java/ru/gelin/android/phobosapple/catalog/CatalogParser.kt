package ru.gelin.android.phobosapple.catalog

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.info
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.Future

class CatalogParser(
    private val catalogJson: InputStream
) {

    private val log = AnkoLogger(javaClass)

    interface VideoTest {
        fun test(json: JSONObject): Boolean
    }

    /**
     * Reads catalog in a background thread.
     */
    fun read(codec: VideoCodec, resolution: VideoResolution): Future<List<Video>> =
        doAsyncResult {
            readCatalog(codec, resolution)
        }


    private fun readCatalog(codec: VideoCodec, resolution: VideoResolution): List<Video> {
        val catalog = catalogJson.reader().readText()
        val json = JSONObject(catalog)

        val result = mutableListOf<Video>()

        val videos = json.getJSONArray("videos")
        for (i in 0 until videos.length()) {
            val videoObject = videos.getJSONObject(i)
            val variantsList = videoObject.getJSONArray("videos")
            val variantObject = chooseVideoVariant(variantsList, codec, resolution)
            if (variantObject != null) {
                val video = Video(
                    group = videoObject.getString("group"),
                    number = videoObject.getString("number"),
                    name = videoObject.getString("name"),
                    resolution = variantObject.getString("resolution"),
                    codec = variantObject.getString("codec"),
                    url = variantObject.getString("url")
                )
                result.add(video)
            }
        }

        log.info { "Read catalog codec=$codec resolution=$resolution videos=${result.size}" }
        return result
    }

    private fun chooseVideoVariant(variants: JSONArray, codec: VideoCodec, resolution: VideoResolution): JSONObject? {
        for (compatibleResolution in resolution.compatibleResolutions) {
            for (compatibleCodec in codec.compatibleCodecs) {
                return findVideoVariant(variants, compatibleCodec, compatibleResolution) ?: continue
            }
        }
        return null
    }

    private fun findVideoVariant(variants: JSONArray, codec: VideoCodec, resolution: VideoResolution): JSONObject? {
        for (j in 0 until variants.length()) {
            val variant = variants.getJSONObject(j)
            if (codec.test(variant) && resolution.test(variant)) {
                return variant
            }
        }
        return null
    }

}


