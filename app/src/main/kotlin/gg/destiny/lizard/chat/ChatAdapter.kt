package gg.destiny.lizard.chat

import android.graphics.Color
import android.support.v4.text.util.LinkifyCompat
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import android.widget.TextView
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import gg.destiny.lizard.R
import gg.destiny.lizard.text.Spanner
import kotlinx.android.synthetic.main.item_chat_message.view.chat_message_message

fun createChatAdapter() =
    FlexAdapter<Any>().apply {
      register<ChatMessage.Message>(R.layout.item_chat_message) { message, view, _ ->
        message.bind(view.chat_message_message)
      }
    }

private fun ChatMessage.Message.bind(message: TextView) {
  val spanner = Spanner().pushSpan(ForegroundColorSpan(0xFF666666.toInt()))
      .pushSpan(AbsoluteSizeSpan(12, true))
      .append("$formattedTime ")
      .popSpan()
      .popSpan()
      .pushSpan(ForegroundColorSpan(colorForFeatures(features)))
      .append(nick)
      .popSpan()
      .pushSpan(ForegroundColorSpan(0xFFAAAAAA.toInt()))
      .append(": ")

  data.split(' ')
      .forEachIndexed { index, s ->
        val emote = EMOTE_MAP[s]
        if (emote != null) {
          spanner.append(' ')
              .pushPopSpan(emote.span)
              .append(' ')
        } else {
          spanner.append(if (index != 0) " $s" else s)
        }
      }

  message.text = spanner.build()
  LinkifyCompat.addLinks(message, Linkify.WEB_URLS)
}

private fun colorForFeatures(features: List<String>) =
    features.mapNotNull { featureOf(it) }
        .sortedByDescending { it.priority }
        .firstOrNull()?.color ?: Color.WHITE
