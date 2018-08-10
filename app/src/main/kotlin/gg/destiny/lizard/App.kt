package gg.destiny.lizard

import android.app.Application
import android.os.StrictMode
import com.github.ajalt.timberkt.d
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import gg.destiny.lizard.chat.EmoteDrawable
import gg.destiny.lizard.core.chat.ChatModule
import gg.destiny.lizard.core.logging.Logger
import gg.destiny.lizard.core.network.NetworkModule
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

class App : Application() {
  init {
    INSTANCE = this
  }

  companion object {
    private lateinit var INSTANCE: App
    fun get(): App {
      return INSTANCE
    }
  }

  lateinit var appComponent: AppComponent

  override fun onCreate() {
    super.onCreate()
    appComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .chatModule(ChatModule())
        .networkModule(NetworkModule())
        .build()

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
