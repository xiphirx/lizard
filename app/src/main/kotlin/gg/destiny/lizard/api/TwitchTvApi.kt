package gg.destiny.lizard.api.twitch

import com.squareup.moshi.Moshi
import gg.destiny.lizard.BuildConfig
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

class TwitchTvApi(okHttp: OkHttpClient, moshi: Moshi) {
  companion object {
    const val BASE_URL = "https://api.twitch.tv"
  }

  private interface Endpoints {
    @Headers("Client-ID: ${BuildConfig.TWITCH_CLIENT_ID}")
    @GET("kraken/streams/{streamId}")
    fun getStreamInformation(@Path("streamId") streamId: String): Observable<StreamInformation>
  }

  private val endpoints: Endpoints

  init {
    val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp.newBuilder()
            .addInterceptor(interceptor)
            .build())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    endpoints = retrofit.create(Endpoints::class.java)
  }

  fun getStreamInformation(channelId: String) = endpoints.getStreamInformation(channelId)
}

data class StreamInformation(val stream: Stream?)

data class Stream(val _id: Long,
                  val game: String,
                  val viewers: Int,
                  val video_height: Int,
                  val average_fps: Float,
                  val delay: Int,
                  val preview: StreamPreview,
                  val channel: StreamChannel)

data class StreamPreview(val small: String, val medium: String, val large: String)

data class StreamChannel(val status: String, val logo: String)


