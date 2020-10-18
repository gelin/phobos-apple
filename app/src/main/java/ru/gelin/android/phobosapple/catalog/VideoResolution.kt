package ru.gelin.android.phobosapple.catalog

import org.json.JSONObject

sealed class VideoResolution: CatalogParser.VideoTest {
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
