package gg.destiny.lizard.core.chat

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
class ChatModule {
  companion object {
    private const val DGG_ENDPOINT = "dgg-endpoint"
    private const val CHAT_GUI_ENDPOINT = "chat-gui-endpoint"
  }

  @Provides
  @Singleton
  @Named(DGG_ENDPOINT)
  fun provideDggEndpoint(): String {
    return "wss://www.destiny.gg/ws"
  }

  @Provides
  @Singleton
  @Named(CHAT_GUI_ENDPOINT)
  fun provideChatGuiEndpoint(): String {
    return "https://raw.githubusercontent.com/destinygg/chat-gui/master/"
  }

  @Provides
  @Singleton
  fun provideChat(socket: ChatSocket, guiApi: ChatGuiApi, storage: ChatStorage): Chat {
    return Chat(socket, guiApi, storage)
  }

  @Provides
  @Singleton
  fun provideChatSocket(
      @Named(DGG_ENDPOINT) endpoint: String,
      okHttpClient: OkHttpClient,
      moshi: Moshi,
      cookieJar: CookieJar
  ): ChatSocket {
    val newMoshi = moshi.newBuilder().add(ChatSocket.Message.Error).build()
    return ChatSocket(endpoint, okHttpClient, newMoshi, cookieJar)
  }

  @Provides
  @Singleton
  fun provideChatGuiApi(
      @Named(CHAT_GUI_ENDPOINT) endpoint: String,
      okHttpClient: OkHttpClient,
      moshi: Moshi
  ): ChatGuiApi {
    return ChatGuiApi(endpoint, okHttpClient, moshi)
  }
}
