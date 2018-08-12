package ru.gelin.android.phobosapple

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : Activity() {

    private val log = AnkoLogger(javaClass)

    private lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initPlayer()

        find<View>(R.id.content).setOnClickListener { playNextMovie() }
    }

    private fun initPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        player.setVideoSurfaceView(find(R.id.video))

        loadVideos()

        player.addListener(object: Player.DefaultEventListener() {
            private fun showLocation() {
                val location = (player.currentTag as? Video)?.location ?: return
                longToast(location)
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
            toast(R.string.loading)
            try {
                val videos = VideosRepository().loadVideos().await()
                val builder = MediaSourceBuilder(this@MainActivity)
                player.prepare(builder.build(videos))
                player.playWhenReady = true
                player.repeatMode = Player.REPEAT_MODE_ALL
            } catch (e: Exception) {
                log.error("Failed to load", e)
                longToast(getString(R.string.load_failure, e.localizedMessage))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                playNextMovie()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                playPrevMovie()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun playNextMovie() {
        player.seekToDefaultPosition(player.nextWindowIndex)
    }

    private fun playPrevMovie() {
        player.seekToDefaultPosition(player.previousWindowIndex)
    }

}
