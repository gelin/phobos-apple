package ru.gelin.android.phobosapple

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import org.jetbrains.anko.*

class PhobosPlayer(
    private val context: Context
) {

    private val log = AnkoLogger(javaClass)

    private lateinit var player: SimpleExoPlayer

    fun init(surfaceView: SurfaceView) {
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)
        player = SimpleExoPlayer.Builder(context).setBandwidthMeter(bandwidthMeter).setTrackSelector(trackSelector).build()
        player.setVideoSurfaceView(surfaceView)

        loadVideos()

        player.addListener(object: Player.EventListener {
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
        doAsync {
            uiThread {
                context.toast(R.string.loading)
            }
            try {
                val videos = VideosRepository(context).loadVideos().get()
                val builder = MediaSourceBuilder(context)
                uiThread {
                    player.prepare(builder.build(videos))
                    player.playWhenReady = true
                    player.repeatMode = Player.REPEAT_MODE_ALL
                }
            } catch (e: Exception) {
                log.error("Failed to load", e)
                uiThread {
                    context.longToast(
                        context.getString(R.string.load_failure, e.localizedMessage)
                    )
                }
            }
        }
    }

    private fun showLocation() {
        context.runOnUiThread {
            log.info("Playing ${player.currentTag}")
            (player.currentTag as? Video)?.name?.let {
                longToast(it)
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
