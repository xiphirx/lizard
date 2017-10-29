package gg.destiny.lizard.api

import gg.destiny.lizard.BuildConfig
import io.reactivex.Observable
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

class DestinyApi(okHttpClient: OkHttpClient, cookieJar: CookieJar) {
  companion object {
    const val HOST = "https://www.destiny.gg"
    // For some reason the redirect isnt https...
    const val REDIRECT_HOST = "http://www.destiny.gg"

    fun oauthRedirectUri(service: LoginService) = "$REDIRECT_HOST/auth/${service.key}"
  }

  enum class LoginService(val key: String) {
    TWITCH("twitch")
  }

  private interface Endpoints {
    /** Initiates an OAuth login through destiny.gg. Returns the redirect response */
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("authProvider") service: String,
        @Field("rememberme") rememberMe: Boolean = true): Observable<Response<String>>

    /** Authenticates the user and eventually sets authorization cookies */
    @GET
    fun auth(@Url path: String): Observable<Response<String>>
  }

  private val endpoints: Endpoints

  init {
    val okHttp = okHttpClient.newBuilder()
        .followRedirects(true)
        .cookieJar(cookieJar)
        .apply {
          if (BuildConfig.DEBUG) {
            addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
          }
        }
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(HOST)
        .client(okHttp)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    endpoints = retrofit.create(Endpoints::class.java)
  }

  fun initiateLogin(service: LoginService) = endpoints.login(service.key)

  // This is dumb, but instead of rewriting a Rx Observable for an OkHttp call, we re-create
  // the query which is far simpler
  fun completeLogin(redirectUrl: String) = endpoints.auth(redirectUrl.substringAfter(REDIRECT_HOST))
}
