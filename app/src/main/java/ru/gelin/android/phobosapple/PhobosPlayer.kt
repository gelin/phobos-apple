package ru.gelin.android.phobosapple

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class PhobosPlayer(
    private val context: Context
) {

    private val log = AnkoLogger(javaClass)

    private lateinit var player: SimpleExoPlayer

    fun init(surfaceView: SurfaceView) {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        player.setVideoSurfaceView(surfaceView)

        loadVideos()

        player.addListener(object: Player.DefaultEventListener() {
            private fun showLocation() {
                val location = (player.currentTag as? Video)?.location ?: return
                context.longToast(location)
            }
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) = when (playbackState) {
                Player.STATE_READY -> showLocation()
                else -> Unit
            }
            override fun onPositionDiscontinuity(reason: Int) = when (reason) {
                Player.DISCONTINUITY_REASON_PERIOD_TRANSITION -> showLocation()
                else -> Unit
            }
        })
    }

    private fun loadVideos() {
        async(UI) {
            this@PhobosPlayer.context.toast(R.string.loading)
            try {
                val videos = VideosRepository().loadVideos().await()
                val builder = MediaSourceBuilder(this@PhobosPlayer.context)
                player.prepare(builder.build(videos))
                player.playWhenReady = true
                player.repeatMode = Player.REPEAT_MODE_ALL
            } catch (e: Exception) {
                log.error("Failed to load", e)
                this@PhobosPlayer.context.longToast(
                    this@PhobosPlayer.context.getString(R.string.load_failure, e.localizedMessage))
            }
        }
    }

    fun playNextMovie() {
        player.seekToDefaultPosition(player.nextWindowIndex)
    }

    fun playPrevMovie() {
        player.seekToDefaultPosition(player.previousWindowIndex)
    }

    fun release() {
        player.release()
    }

}
