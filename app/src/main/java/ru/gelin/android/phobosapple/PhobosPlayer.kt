package ru.gelin.android.phobosapple

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
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
//            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) = when (playbackState) {
//                Player.STATE_READY -> showName()
//                else -> Unit
//            }
//            override fun onPositionDiscontinuity(reason: Int) = when (reason) {
//                Player.DISCONTINUITY_REASON_PERIOD_TRANSITION -> showName()
//                else -> Unit
//            }
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

    private fun showName(mediaItem: MediaItem?) {
        context.runOnUiThread {
            log.info("Playing ${mediaItem?.playbackProperties?.tag}")
            (mediaItem?.playbackProperties?.tag as? Video)?.name?.let {
                // TODO: display differently?
                longToast(it)
            }
        }
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
