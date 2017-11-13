package gg.destiny.lizard.chat

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonReader
import org.joda.time.DateTime
import kotlin.reflect.KClass

sealed class ChatMessage {
  companion object {
    fun of(key: String): KClass<out ChatMessage> = when (key) {
      "NAMES" -> Names::class
      "MSG" -> Message::class
      "QUIT" -> Quit::class
      "JOIN" -> Join::class
      "ERR" -> Error::class
      else -> Unknown::class
    }
  }

  data class Names(val connectioncount: Int) : ChatMessage()

  data class OutgoingMessage(@Json(name = "data") val data: String) : ChatMessage()

  data class Message(
      @Json(name = "nick") val nick: String,
      @Json(name = "features") val features: List<String>,
      @Json(name = "timestamp") val timestamp: Long,
      @Json(name = "data") val data: String
  ) : ChatMessage() {
    @Transient val formattedTime = DateTime(timestamp).run { "$hourOfDay:$minuteOfHour" }
  }

  data class Join(
      @Json(name = "nick") val nick: String,
      @Json(name = "features") val features: List<String>,
      @Json(name = "timestamp") val timestamp: Long
  ) : ChatMessage()

  data class Quit(
      @Json(name = "nick") val nick: String,
      @Json(name = "features") val features: List<String>,
      @Json(name = "timestamp") val timestamp: Long
  ) : ChatMessage()

  data class Error(val message: String) : ChatMessage() {
    companion object {
      @FromJson
      fun fromJson(value: String) = Error(value)
    }
  }

  class Unknown : ChatMessage()
}

