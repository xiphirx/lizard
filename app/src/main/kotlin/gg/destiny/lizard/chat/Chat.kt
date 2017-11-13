package gg.destiny.lizard.chat

import com.github.ajalt.timberkt.d
import com.jakewharton.rxrelay2.PublishRelay
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import gg.destiny.lizard.App
import gg.destiny.lizard.base.extensions.fullMessage
import io.reactivex.Observable
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class Chat(
    private val endpoint: String = DGG_ENDPOINT,
    private val okHttpClient: OkHttpClient = App.okHttp,
    private val moshi: Moshi =
        Moshi.Builder().add(ChatMessage.Error).add(KotlinJsonAdapterFactory()).build(),
    private val cookieJar: CookieJar = App.accountCookieJar
) : WebSocketListener() {
  companion object {
    private const val DGG_ENDPOINT = "wss://www.destiny.gg/ws"
    private const val UNKNOWN_TYPE = "UNK"
  }

  private val relay = PublishRelay.create<ChatMessage>()
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
  }

  fun messages(): Observable<ChatMessage> = relay

  fun sendMessage(message: String) {
    webSocket?.let {
      val outgoingMessage = ChatMessage.OutgoingMessage(message)
      val json = moshi.adapter(ChatMessage.OutgoingMessage::class.java).toJson(outgoingMessage)
      d { "MSG $json"}
      webSocket?.send("MSG $json")
    }
  }

  override fun onMessage(webSocket: WebSocket, message: String) {
    super.onMessage(webSocket, message)
    d { message }
    val type = ChatMessage.of(message.substringBefore(" ", UNKNOWN_TYPE))
    val data = message.substringAfter(' ')
    relay.accept(moshi.adapter(type.java).fromJson(data)!!)
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    super.onFailure(webSocket, t, response)
    d { "failed ${t.fullMessage()}"}
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    super.onClosed(webSocket, code, reason)
    d { "closed" }
    disconnect()
  }
}

