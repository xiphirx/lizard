package gg.destiny.lizard.stream

import gg.destiny.lizard.chat.ChatMessage
import io.reactivex.Observable

sealed class StreamModel {
  data class Online(
      val title: String,
      val viewerCount: Int,
      val chatMessages: Observable<ChatMessage>,
      val url: String
  ) : StreamModel()

  data class Offline(
      val chatMessages: Observable<ChatMessage>
  ) : StreamModel()

  object Loading : StreamModel()
}

