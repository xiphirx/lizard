package gg.destiny.lizard.stream

import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatSocket
import gg.destiny.lizard.core.chat.emptyPackage
import io.reactivex.Observable

data class StreamModel(
    val streamStatus: StreamStatus = StreamStatus.Loading,
    val chatParticipationStatus: ChatParticipationStatus = ChatParticipationStatus.Offline,
    val chatGuiPackage: ChatGuiPackage = emptyPackage
)

sealed class StreamStatus {
  data class Online(
      val title: String,
      val viewerCount: Int,
      val chatMessages: Observable<ChatSocket.Message>,
      val url: String
  ) : StreamStatus()
  data class Offline(val chatMessages: Observable<ChatSocket.Message>) : StreamStatus()
  object Loading : StreamStatus()
}

sealed class ChatParticipationStatus {
  data class Online(val accountInfo: AccountInfo) : ChatParticipationStatus()
  object Offline : ChatParticipationStatus()
  object Banned : ChatParticipationStatus()
}
