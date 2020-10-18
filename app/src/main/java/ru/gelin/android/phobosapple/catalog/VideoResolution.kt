package ru.gelin.android.phobosapple.catalog

import org.json.JSONObject

sealed class VideoResolution: CatalogParser.VideoTest, Comparable<VideoResolution> {
    abstract val compatibleResolutions: List<VideoResolution>

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    override fun compareTo(other: VideoResolution): Int {
        if (this == other) {
            return 0
        }
        if (this == FULLHD) {
            return -1
        }
        if (this == UHD1) {
            return 1
        }
        return 0
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
