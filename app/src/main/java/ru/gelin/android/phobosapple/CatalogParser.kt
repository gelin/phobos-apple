package ru.gelin.android.phobosapple

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.info
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Future
import java.util.function.Predicate

class CatalogParser(
    private val catalogJson: InputStream
) {

    private val log = AnkoLogger(javaClass)

    private interface VideoTest {
        fun test(json: JSONObject): Boolean
    }

    enum class VideoType: VideoTest {
        HEVC_FULLHD_SDR {
            override fun test(json: JSONObject): Boolean {
                return json.getString("resolution") == "1920x1080"
                    && json.getString("codec") == "hvc1"
            }
        },
        HEVC_FULLHD_HDR {
            override fun test(json: JSONObject): Boolean {
                return json.getString("resolution") == "1920x1080"
                    && json.getString("codec") == "dvh1"
            }
        },
        HEVC_UHD1_SDR {
            override fun test(json: JSONObject): Boolean {
                return json.getString("resolution") == "3840x2160"
                    && json.getString("codec") == "hvc1"
            }
        },
        HEVC_UHD1_HDR {
            override fun test(json: JSONObject): Boolean {
                return json.getString("resolution") == "3840x2160"
                    && json.getString("codec") == "dvh1"
            }
        }
    }

    /**
     * Reads catalog in a background thread.
     */
    fun read(type: VideoType): Future<List<Video>> =
        doAsyncResult {
            readCatalog(type)
        }


    private fun readCatalog(type: VideoType): List<Video> {
        val catalog = catalogJson.reader().readText()
        val json = JSONObject(catalog)

        val result = mutableListOf<Video>()

        val videos = json.getJSONArray("videos")
        for (i in 0 until videos.length()) {
            val videoObject = videos.getJSONObject(i)
            val variantsList = videoObject.getJSONArray("videos")
            for (j in 0 until variantsList.length()) {
                val variant = variantsList.getJSONObject(j)
                if (type.test(variant)) {
                    val video = Video(
                        group = videoObject.getString("group"),
                        number = videoObject.getString("number"),
                        name = videoObject.getString("name"),
                        resolution = variant.getString("resolution"),
                        codec = variant.getString("codec"),
                        url = variant.getString("url")
                    )
                    result.add(video)
                }
            }
        }

        result.shuffle()
        log.info { "Read catalog type=${type} videos=${result.size}" }
        return result
    }

}


