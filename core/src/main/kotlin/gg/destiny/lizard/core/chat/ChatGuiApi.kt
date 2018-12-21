package gg.destiny.lizard.core.chat

import com.squareup.moshi.Moshi
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.regex.Pattern

class ChatGuiApi(githubBaseUrl: String, cdnBaseUrl: String, okHttpClient: OkHttpClient, moshi: Moshi) {
  private interface CdnEndpoints{
    @GET("{version}/emotes/emotes.json")
    fun emotes(@Path("version") version: String): Observable<List<ApiEmote>>
  }

  private interface GithubEndpoints {
    @GET("package.json")
    fun packageInfo(): Observable<PackageInfo>
  }

  data class PackageInfo(val version: String)

  private val githubEndpoints: GithubEndpoints
  private val cdnEndpoints: CdnEndpoints

  init {
    val githubRetrofit = Retrofit.Builder()
        .baseUrl(githubBaseUrl)
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    githubEndpoints = githubRetrofit.create(GithubEndpoints::class.java)

    val cdnRetrofit = Retrofit.Builder()
        .baseUrl(cdnBaseUrl)
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    cdnEndpoints = cdnRetrofit.create(CdnEndpoints::class.java)
  }

  data class ApiImageData(val url: String, val name: String, val width: Int, val height: Int)
  data class ApiEmote(val prefix: String, val image: List<ApiImageData>)

  fun getEmoteList(version: String): Observable<List<Emote>> = cdnEndpoints.emotes(version)
      .map {
        it.map {
          val imageData = it.image.first()
          Emote(it.prefix, imageData.width, imageData.height, imageData.url)
        }
      }

  fun getPackageInfo() = githubEndpoints.packageInfo()
}
