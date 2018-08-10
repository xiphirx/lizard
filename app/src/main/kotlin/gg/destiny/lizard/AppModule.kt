package gg.destiny.lizard

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import gg.destiny.lizard.account.AccountCookieJar
import gg.destiny.lizard.chat.SharedPreferencesChatStorage
import gg.destiny.lizard.core.chat.ChatStorage
import okhttp3.CookieJar
import javax.inject.Named
import javax.inject.Singleton

/** @deprecated this probably shouldn't exist, but its a holdover until I move stuff into core */
@Module
class AppModule(private val application: App) {
  @Provides
  @Named("debug-flag")
  fun providesDebugFlag(): Boolean {
    return BuildConfig.DEBUG
  }

  @Provides
  @Named("twitch-client-id")
  fun provideTwitchClientId(): String {
    return BuildConfig.TWITCH_CLIENT_ID
  }

  @Provides
  @Singleton
  fun provideMoshi(): Moshi {
    return Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
  }

  @Provides
  @Singleton
  fun provideChatStorage(): ChatStorage {
    return SharedPreferencesChatStorage()
  }

  @Provides
  @Singleton
  fun provideCookieJar(
      moshi: Moshi,
      @Named("cookie-jar") preferences: SharedPreferences
  ): CookieJar {
    return AccountCookieJar(moshi, preferences)
  }

  @Provides
  @Singleton
  @Named("cookie-jar")
  fun provideCookieJarSharedPreferences(): SharedPreferences {
    return application.getSharedPreferences("cookie_jar", Context.MODE_PRIVATE)
  }

  @Provides
  @Singleton
  @Named("account-info")
  fun provideAccountInfoPreferences(): SharedPreferences {
    return application.getSharedPreferences("account_storage", Context.MODE_PRIVATE)
  }

  @Provides
  @Singleton
  @Named("settings")
  fun provideSettingsPreferences(): SharedPreferences {
    return application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
  }

  @Provides
  @Named("stream-key")
  fun providesStreamKey(): String {
    return "destiny"
  }
}
