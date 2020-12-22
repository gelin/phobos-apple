package ru.gelin.android.phobosapple.catalog

import org.json.JSONObject

sealed class VideoCodec: CatalogParser.VideoTest {
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
