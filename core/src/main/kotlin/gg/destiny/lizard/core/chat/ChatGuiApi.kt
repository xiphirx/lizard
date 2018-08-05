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
import java.util.regex.Pattern

class ChatGuiApi(baseUrl: String = BASE_URL, okHttpClient: OkHttpClient, moshi: Moshi) {
  companion object {
    private const val BASE_URL = "https://raw.githubusercontent.com/destinygg/chat-gui/master/"
    private val PATTERN_EMOTE_BLOCK =
        Pattern.compile(
            "\\.chat-emote\\.chat-emote-([A-Za-z0-9]*)\\s*\\{([a-zA-Z:;0-9\\s\\-]*)\\}",
            Pattern.DOTALL or Pattern.MULTILINE)
    private val PATTERN_POSITION =
        Pattern.compile("background-position:\\s*([\\-0-9]*)(?:px)?\\s*([\\-0-9]*)(?:px)?")
    private val PATTERN_WIDTH = Pattern.compile("width:\\s*([0-9]*)(?:px)?")
    private val PATTERN_HEIGHT = Pattern.compile("height:\\s*([0-9]*)(?:px)?")
  }

  private interface Endpoints {
    @GET("assets/emotes/emoticons.scss")
    fun emoteList(): Observable<String>

    @GET("assets/emotes/emoticons.png")
    fun emoteTexture(): Observable<ResponseBody>

    @GET("package.json")
    fun packageInfo(): Observable<PackageInfo>
  }

  data class PackageInfo(val version: String)

  private val endpoints: Endpoints

  init {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    endpoints = retrofit.create(Endpoints::class.java)
  }

  fun getEmoteList(): Observable<List<Emote>> = endpoints.emoteList().map { parseScss(it) }

  fun getEmoteTexture() = endpoints.emoteTexture()

  fun getPackageInfo() = endpoints.packageInfo()

  private fun parseScss(scss: String): List<Emote> {
    val emoteList = mutableListOf<Emote>()
    val blockMatcher = PATTERN_EMOTE_BLOCK.matcher(scss)
    while (blockMatcher.find()) {
      val name = blockMatcher.group(1)
      val block = blockMatcher.group(2)

      val positionMatcher = PATTERN_POSITION.matcher(block)
      if (!positionMatcher.find()) {
        throw IllegalStateException("$block doesnt match position matcher")
      }
      val x = Integer.parseInt(positionMatcher.group(1))
      val y = Integer.parseInt(positionMatcher.group(2))

      val widthMatcher = PATTERN_WIDTH.matcher(block)
      if (!widthMatcher.find()) {
        throw IllegalStateException("$block doesnt match width matcher")
      }
      val width = Integer.parseInt(widthMatcher.group(1))

      val heightMatcher = PATTERN_HEIGHT.matcher(block)
      if (!heightMatcher.find()) {
        throw IllegalStateException("$block doesnt match height matcher")
      }
      val height = Integer.parseInt(heightMatcher.group(1))
      emoteList.add(Emote(name, x, y, width, height))
    }
    return emoteList
  }
}
