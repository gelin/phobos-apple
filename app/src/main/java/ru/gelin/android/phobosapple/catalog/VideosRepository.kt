package ru.gelin.android.phobosapple.catalog

import android.content.Context
import android.media.MediaCodec
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer
import org.jetbrains.anko.*
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

        // TODO: choose another resolution based on current device capabilities
        val codec = getSupportedCodec()
        val resolution = VideoResolution.FULLHD

        log.info("Loading catalog codec=$codec resolution=$resolution")

        return CatalogParser(catalog).read(codec, resolution).get()
    }

    private val FORMAT_BUILDER = Format.Builder()
        .setWidth(1920).setHeight(1080)
    private val HEVC_HDR_FORMAT = FORMAT_BUILDER
        .setSampleMimeType("video/dolby-vision")
        .build()
    private val HEVC_FORMAT = FORMAT_BUILDER
        .setSampleMimeType("video/hevc")
        .build()
    private val H264_FORMAT = FORMAT_BUILDER
        .setSampleMimeType("video/avc")
        .build()

    private fun getSupportedCodec(): VideoCodec {
        if (isFormatSupported(HEVC_HDR_FORMAT)) {
            log.info("HEVC HDR is supported")
            return VideoCodec.HEVC_HDR
        }
        if (isFormatSupported(HEVC_FORMAT)) {
            log.info("HEVC is supported")
            return VideoCodec.HEVC
        }
        if (isFormatSupported(H264_FORMAT)) {
            log.info("H264 is supported")
            return VideoCodec.H264
        }

        log.warn("No good hardware codec found")
        context.runOnUiThread {
            context.longToast(
                context.getString(R.string.no_supported_codec)
            )
        }
        return VideoCodec.H264
    }

    private fun isFormatSupported(format: Format): Boolean {
        val codec = MediaCodecUtil.getDecoderInfo(format.sampleMimeType!!, false, false)
        log.info("Found codec format=$format codec=$codec")
        return codec?.hardwareAccelerated == true
            && codec.isFormatSupported(format)
    }

}


