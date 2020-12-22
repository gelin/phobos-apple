package ru.gelin.android.phobosapple

import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.gelin.android.phobosapple.catalog.CatalogParser
import ru.gelin.android.phobosapple.catalog.VideoCodec
import ru.gelin.android.phobosapple.catalog.VideoResolution

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CatalogParserTest {

    private lateinit var parser: CatalogParser

    @Before
    fun setUp() {
        parser = CatalogParser(this.javaClass.getResourceAsStream("/test_videos.json")!!)
    }

    @Test
    fun testHdr4k() {
        val videos = parser.read(VideoCodec.HEVC_HDR, VideoResolution.UHD1).get()

        assertEquals(6, videos.size)
        assertEquals(listOf(
            "test5_dvh1_3840x2160",
            "test4_hvc1_3840x2160",
            "test3_dvh1_1920x1080",
            "test2_hvc1_1920x1080",
            "test1_acv1_1920x1080",
            "testH_hvc1_3840x2160"
        ), videos.map { it.url })
    }

    @Test
    fun testHdrFullhd() {
        val videos = parser.read(VideoCodec.HEVC_HDR, VideoResolution.FULLHD).get()

        assertEquals(6, videos.size)
        assertEquals(listOf(
            "test5_dvh1_1920x1080",
            "test4_dvh1_1920x1080",
            "test3_dvh1_1920x1080",
            "test2_hvc1_1920x1080",
            "test1_acv1_1920x1080",
            "testH_hvc1_1920x1080"
        ), videos.map { it.url })
    }

    @Test
    fun testHevc4k() {
        val videos = parser.read(VideoCodec.HEVC, VideoResolution.UHD1).get()

        assertEquals(6, videos.size)
        assertEquals(listOf(
            "test5_hvc1_3840x2160",
            "test4_hvc1_3840x2160",
            "test3_hvc1_1920x1080",
            "test2_hvc1_1920x1080",
            "test1_acv1_1920x1080",
            "testH_hvc1_3840x2160"
        ), videos.map { it.url })
    }

    @Test
    fun testHevcFullhd() {
        val videos = parser.read(VideoCodec.HEVC, VideoResolution.FULLHD).get()

        assertEquals(6, videos.size)
        assertEquals(listOf(
            "test5_hvc1_1920x1080",
            "test4_hvc1_1920x1080",
            "test3_hvc1_1920x1080",
            "test2_hvc1_1920x1080",
            "test1_acv1_1920x1080",
            "testH_hvc1_1920x1080"
        ), videos.map { it.url })
    }

    @Test
    fun testH264Fullhd() {
        val videos = parser.read(VideoCodec.H264, VideoResolution.FULLHD).get()

        assertEquals(5, videos.size)
        assertEquals(listOf(
            "test5_acv1_1920x1080",
            "test4_acv1_1920x1080",
            "test3_acv1_1920x1080",
            "test2_acv1_1920x1080",
            "test1_acv1_1920x1080"
        ), videos.map { it.url })
    }

}
