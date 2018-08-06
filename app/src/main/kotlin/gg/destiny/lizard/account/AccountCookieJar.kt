package gg.destiny.lizard.account

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import gg.destiny.lizard.App
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

data class SerializableCookie(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: String,
    @Json(name = "expires_at") val expiresAt: Long,
    @Json(name = "domain") val domain: String,
    @Json(name = "path") val path: String,
    @Json(name = "secure") val secure: Boolean,
    @Json(name = "http_only") val httpOnly: Boolean,
    @Json(name = "host_only") val hostOnly: Boolean
) {
  companion object {
    fun of(cookie: Cookie) =
        SerializableCookie(
            name = cookie.name(),
            value = cookie.value(),
            expiresAt = cookie.expiresAt(),
            domain = cookie.domain(),
            path = cookie.path(),
            secure = cookie.secure(),
            httpOnly = cookie.httpOnly(),
            hostOnly = cookie.hostOnly())
  }

  fun toCookie(): Cookie = Cookie.Builder()
      .name(name)
      .value(value)
      .expiresAt(expiresAt)
      .path(path).apply {
        if (secure) secure()
        if (httpOnly) httpOnly()
        if (hostOnly) hostOnlyDomain(domain) else domain(domain)
      }
      .build()
}

class AccountCookieJar(
    moshi: Moshi = App.moshi,
    private val preferences: SharedPreferences =
      App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE),
    private val hostFilter: (String) -> Boolean = { true }
) : CookieJar {
  companion object {
    const val PREF_NAME = "cookie_jar"
    const val COOKIES_KEY = "cookies"
  }

  private val typeAdapter = moshi.adapter(SerializableCookie::class.java)
  private var cookieJar = mutableMapOf<String, List<Cookie>>()

  override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
    if (!hostFilter(url.host())) return

    val key = prefKey(url.host())
    val newCookies = cookies.map {
      typeAdapter.toJson(SerializableCookie.of(it))
    }

    cookieJar[key] = cookies
    preferences.edit().putStringSet(key, newCookies.toSet()).apply()
  }

  override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
    val cookies = mutableListOf<Cookie>()
    val host = url.host()
    if (!hostFilter(host)) return cookies

    val key = prefKey(host)
    var existingCookies = cookieJar[key]
    if (existingCookies != null) {
      cookies.addAll(existingCookies)
      return cookies
    }

    existingCookies = loadCookieListFromDisk(host)
    if (existingCookies.isNotEmpty()) {
      cookies.addAll(existingCookies)
    }

    return cookies
  }

  private fun loadCookieListFromDisk(host: String): List<Cookie> {
    val newCookieList = mutableListOf<Cookie>()
    val key = prefKey(host)
    preferences.getStringSet(key, emptySet()).forEach {
      val serializableCookie = typeAdapter.fromJson(it) ?: return@forEach
      newCookieList.add(serializableCookie.toCookie())
    }
    cookieJar[key] = newCookieList
    return newCookieList
  }

  private fun prefKey(host: String) = "$COOKIES_KEY-$host"
}

