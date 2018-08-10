package gg.destiny.lizard.core.api

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApiModule {
  @Provides
  @Singleton
  fun provideDestinyApi(
      @Named("debug-flag") isDebug: Boolean,
      okHttpClient: OkHttpClient,
      cookieJar: CookieJar,
      moshi: Moshi
  ): DestinyApi {
    return DestinyApi(okHttpClient, cookieJar, moshi, isDebug)
  }

  @Provides
  @Singleton
  fun provideTwitchTvApi(
      @Named("twitch-client-id") clientId: String,
      okHttpClient: OkHttpClient,
      moshi: Moshi
  ): TwitchTvApi {
    return TwitchTvApi(clientId, okHttpClient, moshi)
  }
}
