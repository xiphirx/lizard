package gg.destiny.lizard.chat

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.text.util.LinkifyCompat
import android.text.style.ForegroundColorSpan
import android.text.util.Linkify
import android.widget.TextView
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import gg.destiny.lizard.R
import gg.destiny.lizard.account.AccountFeature
import gg.destiny.lizard.base.text.Spanner
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatSocket
import kotlinx.android.synthetic.main.item_chat_message.view.chat_message_message

fun createChatAdapter(chatGuiPackage: () -> ChatGuiPackage, highlightNick: () -> String?) =
    FlexAdapter<Any>().apply {
      register<ChatSocket.Message.UserMessage>(R.layout.item_chat_message) { message, view, _ ->
        message.bind(chatGuiPackage(), view.chat_message_message)

        val nick = highlightNick()
        val data = message.data
        view.setBackgroundColor(
            if (nick != null && (data.contains(" $nick ") || data.startsWith("$nick "))) {
              ContextCompat.getColor(view.context, R.color.white10)
            } else {
              Color.TRANSPARENT
            }
        )
      }
    }

private fun ChatSocket.Message.UserMessage.bind(packageInfo: ChatGuiPackage, message: TextView) {
  val spanner = Spanner()
      .pushSpan(ForegroundColorSpan(colorForFeatures(features)))
      .append(nick)
      .popSpan()
      .pushSpan(ForegroundColorSpan(0xFFAAAAAA.toInt()))
      .append(": ")

  data.split(' ')
      .forEachIndexed { index, s ->
        val emote = packageInfo.emoteMap[s]
        if (emote != null) {
          spanner.append(' ')
              .pushPopSpan(EmoteSpan(emote))
              .append(' ')
        } else {
          spanner.append(if (index != 0) " $s" else s)
        }
      }

  message.text = spanner.build()
  LinkifyCompat.addLinks(message, Linkify.WEB_URLS)
}

private fun colorForFeatures(features: List<String>) =
    features.map { AccountFeature.of(it) }
        .sortedByDescending { it.priority }
        .firstOrNull()?.color ?: Color.WHITE
