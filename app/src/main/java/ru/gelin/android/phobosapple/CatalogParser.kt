package ru.gelin.android.phobosapple

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

    private interface VideoTest {
        fun test(json: JSONObject): Boolean
    }

    sealed class VideoCodec: VideoTest {
        abstract val compatibleCodecs: List<VideoCodec>

        override fun toString(): String {
            return this.javaClass.simpleName
        }

        object H264: VideoCodec() {
            override fun test(json: JSONObject): Boolean {
                return json.getString("codec") == "avc1"
            }
            override val compatibleCodecs = listOf(H264)
        }

        object HEVC: VideoCodec() {
            override fun test(json: JSONObject): Boolean {
                return json.getString("codec") == "hvc1"
            }
            override val compatibleCodecs = listOf(HEVC, H264)
        }

        object HEVC_HDR: VideoCodec() {
            override fun test(json: JSONObject): Boolean {
                return json.getString("codec") == "dvh1"
            }
            override val compatibleCodecs = listOf(HEVC_HDR, HEVC, H264)
        }
    }

    sealed class VideoResolution: VideoTest {
        abstract val compatibleResolutions: List<VideoResolution>

        override fun toString(): String {
            return this.javaClass.simpleName
        }

        object FULLHD: VideoResolution() {
            override fun test(json: JSONObject): Boolean {
                return json.getString("resolution") == "1920x1080"
            }
            override val compatibleResolutions = listOf(FULLHD)
        }

        object UHD1: VideoResolution() {
            override fun test(json: JSONObject): Boolean {
                return json.getString("resolution") == "3840x2160"
            }
            override val compatibleResolutions = listOf(UHD1, FULLHD)
        }
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

        log.info { "Read catalog code=$codec resolution=$resolution videos=${result.size}" }
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


