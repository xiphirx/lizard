package gg.destiny.lizard.core.network

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class NetworkModule {
  @Provides
  @Singleton
  fun provideOkHttp(): OkHttpClient {
    return OkHttpClient()
  }
}
