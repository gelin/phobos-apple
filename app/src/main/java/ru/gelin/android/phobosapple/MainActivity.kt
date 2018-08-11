package ru.gelin.android.phobosapple

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.VideoView
import org.jetbrains.anko.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : Activity() {

    private val log = AnkoLogger(javaClass)

    private val movies = MoviesRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        find<View>(R.id.content).setOnClickListener { playNextMovie() }
    }

    override fun onResume() {
        super.onResume()

        playNextMovieAfterSecond()
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

    private fun playNextMovieAfterSecond() {
        Handler().postDelayed(
            { playNextMovie() },
            1000
        )
    }

    private fun playNextMovie() {
        val movie = movies.nextMovie()
        if (movie == null) {
            toast("No movies loaded")
            playNextMovieAfterSecond()
            return
        }

        log.info { "Playing $movie" }
        longToast(movie.location)

        val videoView = find<VideoView>(R.id.video)
        videoView.setVideoPath(movie.url)
        videoView.start()
    }

}
