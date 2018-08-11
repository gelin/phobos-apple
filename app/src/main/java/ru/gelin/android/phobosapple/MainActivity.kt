package ru.gelin.android.phobosapple

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.toast

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

        playNextMovie()
    }

    private fun playNextMovie() {
        val movie = movies.nextMovie()
        if (movie == null) {
            toast("No movies loaded")
            return
        }

        log.info { "Playing $movie" }

        val videoView = find<VideoView>(R.id.video)
        videoView.setVideoPath(movie.url)
        videoView.start()
    }

}
