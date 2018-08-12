package ru.gelin.android.phobosapple

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MediaSourceBuilder(
    private val context: Context
) {

    fun build(videos: List<Video>): MediaSource {
        val bandwidthMeter = DefaultBandwidthMeter()
        val result = ConcatenatingMediaSource()
        for (video in videos) {
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name)),
                bandwidthMeter
            )
            val videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(video.url))
            // TODO: setTag()
            result.addMediaSource(videoSource)
        }
        return result
    }

}


