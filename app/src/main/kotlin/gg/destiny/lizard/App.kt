package gg.destiny.lizard

import android.app.Application
import android.os.StrictMode
import com.github.ajalt.timberkt.d
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import gg.destiny.lizard.account.AccountCookieJar
import gg.destiny.lizard.account.AccountInfoStorage
import gg.destiny.lizard.account.AccountManager
import gg.destiny.lizard.api.TwitchTvApi
import gg.destiny.lizard.chat.EmoteDrawable
import gg.destiny.lizard.core.logging.Logger
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient
import timber.log.Timber

class App : Application() {
  companion object {
    lateinit var INSTANCE: App
    val okHttp by lazy { OkHttpClient() }
    val accountInfoStorage by lazy { AccountInfoStorage() }
    val accountCookieJar by lazy { AccountCookieJar() }
    val accountManager by lazy { AccountManager() }
    val twitchTv by lazy { TwitchTvApi(okHttp, moshi) }
    val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  }

  override fun onCreate() {
    super.onCreate()
    INSTANCE = this
    if (BuildConfig.DEBUG) {
      StrictMode.enableDefaults()
      Timber.plant(Timber.DebugTree())
      Logger.instance = object : Logger.Instance {
        override fun log(message: String)  = d { message }
        override fun log(t: Throwable, message: String)  = d(t) { message }
      }
    }

//    Thread.setDefaultUncaughtExceptionHandler { _, e ->
//      d(e) { "Uncaught" }
//      throw RuntimeException(e)
//    }

    JodaTimeAndroid.init(this)

    EmoteDrawable.density = resources.displayMetrics.density
  }
}
