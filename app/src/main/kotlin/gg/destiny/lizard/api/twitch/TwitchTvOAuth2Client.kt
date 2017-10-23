package gg.destiny.lizard.api.twitch

import gg.destiny.lizard.App
import gg.destiny.lizard.api.oauth2.OAuth2Client
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.SecureRandom

class TwitchTvOAuth2Client(
    private val clientId: String = "hk3xmf0uwmsdhve6ylr4kjbp538pr6",
    override val redirectSlug: String = "gg.destiny.lizard://oauth.twitch",
    private val scopes: List<String> = listOf("user:edit"),
    private val okHttpClient: OkHttpClient = App.OKHTTP
) : OAuth2Client {

  private interface Endpoints {
    @GET("kraken/oauth2/authorize")
    fun authorize(
        @Query("client_id") clientId: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("scope") scopes: String,
        @Query("state") state: String
    ): Observable<Unit>
  }

  private val secureRandom = SecureRandom()
  private val endpoints: Endpoints
  private var state: String = generateState()

  init {
    val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    val retrofit = Retrofit.Builder()
        .baseUrl(TwitchTvApi.BASE_URL)
        .client(okHttpClient.newBuilder()
            .addInterceptor(interceptor)
            .build())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    endpoints = retrofit.create(Endpoints::class.java)
  }

  override val authorizeUrl: String
    get() {
      state = generateState()
      return "${TwitchTvApi.BASE_URL}/kraken/oauth2/authorize?client_id=$clientId&redirect_uri=$redirectSlug&scope=user:edit&state=$state"
    }

  val getCurrentState = state

  private fun generateState(): String {
    val bytes = ByteArray(64)
    secureRandom.nextBytes(bytes)
    return String(bytes)
  }
}
