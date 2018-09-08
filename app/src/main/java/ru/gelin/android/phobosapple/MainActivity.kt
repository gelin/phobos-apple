package ru.gelin.android.phobosapple

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : Activity() {

    private val log = AnkoLogger(javaClass)

    private var player = PhobosPlayer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        player.init(find(R.id.video))

        find<View>(R.id.content).setOnClickListener {
            player.playNextMovie()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                player.playNextMovie()
                true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                player.playPrevMovie()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

}
