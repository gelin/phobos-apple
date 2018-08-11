package ru.gelin.android.phobosapple

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : Activity() {

    private val movies = MoviesRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

//        fullscreen_content.setOnClickListener { toggle() }
    }

    override fun onResume() {
        super.onResume()

        val text = StringBuilder()
        for (i in 0 until 10) {
            val movie = movies.nextMovie() ?: continue
            text.append(movie.location)
            text.append("\n")
            text.append(movie.url)
            text.append("\n")
        }

        val moviesList = findViewById<TextView>(R.id.movie_list)
        moviesList.text = text
    }

}
