package ru.gelin.android.phobosapple

import android.service.dreams.DreamService
import org.jetbrains.anko.AnkoLogger

class PhobosDreamService: DreamService() {

    private val log = AnkoLogger(javaClass)

    private var player = PhobosPlayer(this)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = false
        isFullscreen = true
        isScreenBright = true

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
