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

        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        player.setVideoSurfaceView(find(R.id.video))

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

        find<View>(R.id.content).setOnClickListener { playNextMovie() }
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                playNextMovie()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun playNextMovie() {
//        val movie = videos.nextMovie()
//        if (movie == null) {
//            toast("No videos loaded")
//            playNextMovieAfterSecond()
//            return
//        }
//
//        log.info { "Playing $movie" }
//
//        val videoView = find<VideoView>(R.id.video)
//        videoView.setVideoPath(movie.url)
//        videoView.start()
//        videoView.setOnPreparedListener { longToast(movie.location) }
    }

}
