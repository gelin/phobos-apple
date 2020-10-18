package ru.gelin.android.phobosapple

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlaybackException.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_IDLE
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ShuffleOrder
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import org.jetbrains.anko.*
import ru.gelin.android.phobosapple.catalog.Video
import ru.gelin.android.phobosapple.catalog.VideosRepository
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class PhobosPlayer(
    private val context: Context
) {

    private val log = AnkoLogger(javaClass)

    private lateinit var player: SimpleExoPlayer

    fun init(surfaceView: SurfaceView) {
        player = SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(createHttpDataSourceFactory()))
            .setBandwidthMeter(DefaultBandwidthMeter.Builder(context).build())
            .setTrackSelector(DefaultTrackSelector(context, AdaptiveTrackSelection.Factory()))
            .build()
        player.setVideoSurfaceView(surfaceView)

        loadVideos()

        player.addListener(object: Player.EventListener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                showName(mediaItem)
            }
            override fun onPlayerError(error: ExoPlaybackException) {
                showError(error)
            }
        })
    }

    private fun createHttpDataSourceFactory(): HttpDataSource.Factory {
        // https://stackoverflow.com/a/60507560/3438640
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate?>?,
                                                authType: String?) = Unit
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate?>?,
                                                authType: String?) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        // Install the all-trusting trust manager
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        // TODO: add cache?
        return OkHttpDataSourceFactory(client, Util.getUserAgent(context, context.getString(R.string.app_name)))
    }

    private fun loadVideos() {
        doAsync {
            uiThread {
                context.toast(R.string.loading)
            }
            try {
                val videos = VideosRepository(context).loadVideos().get()
                uiThread {
                    player.addMediaItems(videos.map{ it.toMediaItem() })
                    player.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(videos.size))
                    player.shuffleModeEnabled = true
                    player.repeatMode = Player.REPEAT_MODE_ALL
                    player.prepare()
                    player.next()
                    player.play()
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

    private fun Video.toMediaItem(): MediaItem = MediaItem.Builder().setUri(this.url).setTag(this).build()

    private fun showName(mediaItem: MediaItem?) {
        context.runOnUiThread {
            if (player.playbackState != STATE_IDLE) {
                log.info("Playing ${mediaItem?.playbackProperties?.tag}")
                (mediaItem?.playbackProperties?.tag as? Video)?.name?.let {
                    // TODO: display without the toast?
                    longToast(it)
                }
            }
        }
    }

    private fun showError(error: ExoPlaybackException) {
        log.error("Playback error: ${error.message}")
        val exception = when(error.type) {
            TYPE_OUT_OF_MEMORY -> error.outOfMemoryError
            TYPE_RENDERER -> error.rendererException
            TYPE_SOURCE -> error.sourceException
            TYPE_TIMEOUT -> error.timeoutException
            TYPE_UNEXPECTED -> error.unexpectedException
            else -> Exception("unknown")
        }
        val message = "${error.message}: ${exception.message}"
        context.runOnUiThread {
            longToast(message)
            // TODO: remove broken video from playlist
            player.next()
            player.prepare()
            player.play()
        }
    }

    fun playNext() {
        player.next()
    }

    fun playPrev() {
        player.previous()
    }

    fun release() {
        player.release()
    }

}
