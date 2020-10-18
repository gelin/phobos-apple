package ru.gelin.android.phobosapple.catalog

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.view.Display
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.util.Util
import org.jetbrains.anko.*
import ru.gelin.android.phobosapple.R
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

        val (codec, codecResolution) = getSupportedCodecResolution()
        val displayResolution = getSupportedResolution()
        val resolution = minOf(codecResolution, displayResolution)

        log.info("Loading catalog codec=$codec resolution=$resolution")

        return CatalogParser(catalog).read(codec, resolution).get()
    }

    private val FULLHD_BUILDER = Format.Builder().setWidth(1920).setHeight(1080)
    private val FULLHD_HEVC_HDR_FORMAT = FULLHD_BUILDER.setSampleMimeType("video/dolby-vision").build()
    private val FULLHD_HEVC_FORMAT = FULLHD_BUILDER.setSampleMimeType("video/hevc").build()
    private val FULLHD_H264_FORMAT = FULLHD_BUILDER.setSampleMimeType("video/avc").build()
    private val UHD1_BUILDER = Format.Builder().setWidth(3840).setHeight(2160)
    private val UHD1_HEVC_HDR_FORMAT = UHD1_BUILDER.setSampleMimeType("video/dolby-vision").build()
    private val UHD1_HEVC_FORMAT = UHD1_BUILDER.setSampleMimeType("video/hevc").build()

    private val CHECK_FORMATS = mapOf(
        UHD1_HEVC_HDR_FORMAT to (VideoCodec.HEVC_HDR to VideoResolution.UHD1),
        UHD1_HEVC_FORMAT to (VideoCodec.HEVC to VideoResolution.UHD1),
        FULLHD_HEVC_HDR_FORMAT to (VideoCodec.HEVC_HDR to VideoResolution.FULLHD),
        FULLHD_HEVC_FORMAT to (VideoCodec.HEVC to VideoResolution.FULLHD),
        FULLHD_H264_FORMAT to (VideoCodec.H264 to VideoResolution.FULLHD)
    )

    private fun getSupportedCodecResolution(): Pair<VideoCodec, VideoResolution> {
        for ((format, result) in CHECK_FORMATS.entries) {
            if (isFormatSupported(format)) {
                log.info("Found supported format=$format")
                return result
            }
        }

        log.warn("No good hardware codec found")
        context.runOnUiThread {
            context.longToast(
                context.getString(R.string.no_supported_codec)
            )
        }
        return VideoCodec.H264 to VideoResolution.FULLHD
    }

    private fun isFormatSupported(format: Format): Boolean {
        val codec = MediaCodecUtil.getDecoderInfo(format.sampleMimeType!!, false, false)
        log.info("Found codec format=$format codec=$codec")
        return codec?.hardwareAccelerated == true
            && codec.isFormatSupported(format)
    }

    private fun getSupportedResolution(): VideoResolution {
        val size = Util.getCurrentDisplayModeSize(context)
        log.info("Detected display size=$size");
        return if (size.x >= 2160 && size.y >= 2160) VideoResolution.UHD1   // for any screen direction
            else VideoResolution.FULLHD
    }

}


