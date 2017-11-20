package gg.destiny.lizard.chat

import android.animation.ObjectAnimator
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.text.util.LinkifyCompat
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.text.util.Linkify
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import gg.destiny.lizard.R
import gg.destiny.lizard.account.AccountFeature
import gg.destiny.lizard.base.text.Spanner
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatSocket
import kotlinx.android.synthetic.main.item_chat_message.view.chat_message_message

data class ComboMessage(
    val emoteSpan: EmoteSpan,
    var count: Int = 2,
    var completed: Boolean = false,
    var ticked: Boolean = true
) {
  fun bind(view: TextView) {
    view.text = Spanner()
        .pushPopSpan(emoteSpan)
        .pushSpan(ForegroundColorSpan(Color.WHITE))
        .pushSpan(AbsoluteSizeSpan((view.textSize * (1 + 0.05f * count)).toInt(), false))
        .append(" ${count}x ")
        .popSpan()
        .popSpan()
        .pushSpan(ForegroundColorSpan(0xFFAAAAAA.toInt()))
        .append(if (completed) "C-C-C-COMBO" else "HITS")
        .build()
    ticked = false
  }
}

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

      register<ComboMessage>(R.layout.item_chat_message) { combo, view, _ ->
        combo.bind(view.chat_message_message)
      }
    }

private fun ChatSocket.Message.UserMessage.bind(packageInfo: ChatGuiPackage, message: TextView) {
  val spanner = Spanner()
      .pushSpan(ForegroundColorSpan(colorForFeatures(features)))
      .append(nick)
      .popSpan()
      .pushSpan(
          ForegroundColorSpan(
              if (data.firstOrNull() == '>') 0xFF6CA528.toInt() else 0xFFAAAAAA.toInt()))
      .append(": ")

  data.split(' ')
      .forEachIndexed { index, s ->
        val emote = packageInfo.emoteMap[s]
        if (emote != null) {
          val span = EmoteSpan(emote)
          when (emote.name) {
            "REE", "OverRustle" -> rage(span, message)
            "MLADY" -> tipTip(span, message)
            "DANKMEMES" -> dankHueShift(span, message)
          }
          spanner.append(' ')
              .pushPopSpan(span)
              .append(' ')
        } else {
          spanner.append(if (index != 0) " $s" else s)
        }
      }

  message.text = spanner.build()
  LinkifyCompat.addLinks(message, Linkify.WEB_URLS)
}

private fun tipTip(span: EmoteSpan, view: TextView) {
  ObjectAnimator.ofFloat(span, EmoteSpan.RotateProperty, 0f, 15f, 0f, 15f, 0f).apply {
    addUpdateListener { view.invalidate() }
    interpolator = AccelerateDecelerateInterpolator()
    duration = 500
    start()
  }
}

private fun rage(span: EmoteSpan, view: TextView) {
  ObjectAnimator.ofFloat(span, EmoteSpan.TranslateXProperty, 0f, -5f, 0f, 5f, 0f).apply {
    addUpdateListener { view.invalidate() }
    interpolator = AccelerateDecelerateInterpolator()
    duration = 100
    repeatCount = 3
    start()
  }
}

private fun dankHueShift(span: EmoteSpan, view: TextView) {
  ObjectAnimator.ofFloat(span, EmoteSpan.HueShiftProperty, 0f, 360f, 0f).apply {
    addUpdateListener { view.invalidate() }
    interpolator = AccelerateDecelerateInterpolator()
    duration = 1000
    start()
  }
}

private fun colorForFeatures(features: List<String>) =
    features.map { AccountFeature.of(it) }
        .sortedByDescending { it.priority }
        .firstOrNull()?.color ?: Color.WHITE
