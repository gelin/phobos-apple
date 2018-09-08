package ru.gelin.android.phobosapple

import android.service.dreams.DreamService
import org.jetbrains.anko.AnkoLogger

class PhobosDreamService: DreamService() {

    private val log = AnkoLogger(javaClass)

    private lateinit var player: PhobosPlayer

    override fun onCreate() {
        super.onCreate()

        player = PhobosPlayer(applicationContext)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = false
        isFullscreen = true

        setContentView(R.layout.main)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()

        player.init(findViewById(R.id.video))
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()

        player.release()
    }

}
