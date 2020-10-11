package ru.gelin.android.phobosapple

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class PlaylistBuilder(
    private val dataSourceFactory: DataSource.Factory
) {

    fun build(videos: List<Video>): MediaSource {
        val result = ConcatenatingMediaSource()
        for (video in videos) {
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .setTag(video)
                .createMediaSource(Uri.parse(video.url))
            result.addMediaSource(videoSource)
        }
        return result
    }

}


