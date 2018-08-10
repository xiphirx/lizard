package gg.destiny.lizard.core.chat

import com.jakewharton.rxrelay2.PublishRelay
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import gg.destiny.lizard.core.extensions.fullMessage
import gg.destiny.lizard.core.logging.L
import io.reactivex.Observable
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.reflect.KClass

class ChatSocket(
    private val endpoint: String,
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi,
    private val cookieJar: CookieJar
) : WebSocketListener() {
  companion object {
    private const val UNKNOWN_TYPE = "UNK"
  }

  sealed class Message {
    companion object {
      fun of(key: String): KClass<out Message> = when (key) {
        "NAMES" -> Names::class
        "MSG" -> UserMessage::class
        "QUIT" -> Quit::class
        "JOIN" -> Join::class
        "ERR" -> Error::class
        "BROADCAST" -> Broadcast::class
        "MUTE" -> Mute::class
        "BAN" -> Ban::class
        else -> Unknown::class
      }
    }

    data class Names(val connectioncount: Int) : Message()

    data class OutgoingMessage(@Json(name = "data") val data: String) : Message()

    data class UserMessage(
        @Json(name = "nick") val nick: String,
        @Json(name = "features") val features: List<String>,
        @Json(name = "timestamp") val timestamp: Long,
        @Json(name = "data") val data: String
    ) : Message()

    data class Join(
        @Json(name = "nick") val nick: String,
        @Json(name = "features") val features: List<String>,
        @Json(name = "timestamp") val timestamp: Long
    ) : Message()

    data class Quit(
        @Json(name = "nick") val nick: String,
        @Json(name = "features") val features: List<String>,
        @Json(name = "timestamp") val timestamp: Long
    ) : Message()

    data class Mute(
        @Json(name = "nick") val nick: String,
        @Json(name = "features") val features: List<String>,
        @Json(name = "timestamp") val timestamp: Long,
        @Json(name = "data") val data: String
    ) : Message()

    data class Ban(
        @Json(name = "nick") val nick: String,
        @Json(name = "features") val features: List<String>,
        @Json(name = "timestamp") val timestamp: Long,
        @Json(name = "data") val data: String
    ) : Message()

    data class Broadcast(
        @Json(name = "timestamp") val timestamp: Long,
        @Json(name = "data") val data: String
    ) : Message()

    data class Error(val message: String) : Message() {
      companion object {
        @FromJson
        fun fromJson(value: String) = Error(value)
      }
    }

    object Unknown : Message()
  }

  private val relay = PublishRelay.create<Message>()
  private var webSocket: WebSocket? = null

  fun connect() {
    if (webSocket != null) {
      return
    }

    val request = Request.Builder().url(endpoint).build()
    webSocket = okHttpClient.newBuilder().cookieJar(cookieJar).build().newWebSocket(request, this)
  }

  fun disconnect() {
    webSocket?.cancel()
    webSocket = null
  }

  fun messages(): Observable<Message> = relay

  fun sendMessage(message: String) {
    webSocket?.let {
      val outgoingMessage = Message.OutgoingMessage(message)
      val json = moshi.adapter(Message.OutgoingMessage::class.java).toJson(outgoingMessage)
      webSocket?.send("MSG $json")
    }
  }

  override fun onMessage(webSocket: WebSocket, message: String) {
    super.onMessage(webSocket, message)
    val type = Message.of(message.substringBefore(" ", UNKNOWN_TYPE))
    val data = message.substringAfter(' ')
    L { "Message: $message" }
    relay.accept(moshi.adapter(type.java).fromJson(data)!!)
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    super.onFailure(webSocket, t, response)
    L { "failed ${t.fullMessage()}" }
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    super.onClosed(webSocket, code, reason)
    L { "closed" }
    disconnect()
  }
}

