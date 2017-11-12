package gg.destiny.lizard

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import com.facebook.stetho.Stetho
import com.github.ajalt.timberkt.d
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import gg.destiny.lizard.account.AccountStorage
import gg.destiny.lizard.api.TwitchTvApi
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient
import timber.log.Timber

class App : Application() {
  companion object {
    lateinit var INSTANCE: App
    val okHttp by lazy { OkHttpClient() }
    val accountStorage by lazy { AccountStorage() }
    val twitchTv by lazy { TwitchTvApi(okHttp, moshi) }
    val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  }

  override fun onCreate() {
    super.onCreate()
    INSTANCE = this
    if (BuildConfig.DEBUG) {
      StrictMode.enableDefaults()
      Timber.plant(Timber.DebugTree())
    }

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
      d(e) { "Uncaught" }
      Handler(Looper.getMainLooper()).post {
        throw RuntimeException(e)
      }
    }

    JodaTimeAndroid.init(this)
    if (BuildConfig.DEBUG) {
      Stetho.initializeWithDefaults(this)
    }
  }
}
