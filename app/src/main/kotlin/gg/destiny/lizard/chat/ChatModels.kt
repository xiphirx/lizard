package gg.destiny.lizard.chat

import org.joda.time.DateTime
import kotlin.reflect.KClass

sealed class ChatMessage {
  companion object {
    fun of(key: String): KClass<out ChatMessage> = when (key) {
      "NAMES" -> Names::class
      "MSG" -> Message::class
      "QUIT" -> Quit::class
      "JOIN" -> Join::class
      else -> Unknown::class
    }
  }

  data class Names(val connectioncount: Int) : ChatMessage()

  data class Message(
      val nick: String,
      val features: List<String>,
      val timestamp: Long,
      val data: String
  ) : ChatMessage() {
    @Transient val formattedTime = DateTime(timestamp).run { "$hourOfDay:$minuteOfHour" }
  }

  data class Join(
      val nick: String,
      val features: List<String>,
      val timestamp: Long
  ) : ChatMessage()

  data class Quit(
      val nick: String,
      val features: List<String>,
      val timestamp: Long
  ) : ChatMessage()

  class Unknown : ChatMessage()
}

