package gg.destiny.lizard.stream

import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.chat.ChatMessage
import io.reactivex.Observable

data class StreamModel(
    val streamStatus: StreamStatus = StreamStatus.Loading,
    val chatParticipationStatus: ChatParticipationStatus = ChatParticipationStatus.Offline
)

sealed class StreamStatus {
  data class Online(
      val title: String,
      val viewerCount: Int,
      val chatMessages: Observable<ChatMessage>,
      val url: String
  ) : StreamStatus()
  data class Offline(val chatMessages: Observable<ChatMessage>) : StreamStatus()
  object Loading : StreamStatus()
}

sealed class ChatParticipationStatus {
  data class Online(val accountInfo: AccountInfo) : ChatParticipationStatus()
  object Offline : ChatParticipationStatus()
  object Banned : ChatParticipationStatus()
}
