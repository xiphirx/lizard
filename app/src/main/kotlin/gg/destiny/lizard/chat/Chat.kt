package gg.destiny.lizard.chat

import com.github.ajalt.timberkt.d
import com.jakewharton.rxrelay2.PublishRelay
import com.squareup.moshi.Moshi
import gg.destiny.lizard.App
import gg.destiny.lizard.base.extensions.fullMessage
import io.reactivex.Observable
import okhttp3.*
import okio.ByteString

class Chat(
    private val endpoint: String = DGG_ENDPOINT,
    private val okHttpClient: OkHttpClient = App.okHttp,
    private val moshi: Moshi = App.moshi) : WebSocketListener() {
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
    webSocket = okHttpClient.newWebSocket(request, this)
  }

  fun disconnect() {
    webSocket?.cancel()
  }

  fun messages(): Observable<ChatMessage> = relay

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    super.onMessage(webSocket, bytes)
    d { "bytes $bytes"}
  }

  override fun onMessage(webSocket: WebSocket, message: String) {
    super.onMessage(webSocket, message)
    d { message }
    val type = ChatMessage.of(message.substringBefore(" {", UNKNOWN_TYPE))
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
