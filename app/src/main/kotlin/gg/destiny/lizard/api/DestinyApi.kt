package gg.destiny.lizard.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import gg.destiny.lizard.App
import gg.destiny.lizard.BuildConfig
import io.reactivex.Observable
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

class DestinyApi(okHttpClient: OkHttpClient, cookieJar: CookieJar, moshi: Moshi) {
  companion object {
    const val HOST = "www.destiny.gg"
    const val BASE_URL = "https://$HOST"
    // For some reason the redirect isnt https...
    const val REDIRECT_BASE_URL = "http://$HOST"

    fun oauthRedirectUri(service: LoginService) = "$REDIRECT_BASE_URL/auth/${service.key}"
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
        @Field("rememberme") rememberMe: Boolean = true
    ): Observable<Response<String>>

    /** Authenticates the user and eventually sets authorization cookies */
    @GET
    fun auth(@Url path: String): Observable<Response<String>>

    /** Retrieves information about the current session, if any */
    @GET("api/chat/me")
    fun sessionInformation(): Observable<Response<SessionInformation.Available>>
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
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    endpoints = retrofit.create(Endpoints::class.java)
  }

  fun initiateLogin(service: LoginService) = endpoints.login(service.key)

  // This is dumb, but instead of rewriting a Rx Observable for an OkHttp call, we re-create
  // the query which is far simpler
  fun completeLogin(redirectUrl: String) = endpoints.auth(redirectUrl.substringAfter(REDIRECT_BASE_URL))

  fun getSessionInformation() = endpoints.sessionInformation()
}

sealed class SessionInformation {
  data class Available(
      @Json(name = "email") val email: String,
      @Json(name = "nick") val nick: String,
      @Json(name = "username") val username: String,
      @Json(name = "userId") val userId: String,
      @Json(name = "userStatus") val userStatus: String,
      @Json(name = "roles") val roles: List<String>,
      @Json(name = "features") val features: List<String>,
      @Json(name = "subscription") val subscription: String?,
      @Json(name = "settings") val settings: List<String>
  ) : SessionInformation()

  object Unavailable : SessionInformation()
}
