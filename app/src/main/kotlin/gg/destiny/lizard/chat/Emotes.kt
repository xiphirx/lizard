package gg.destiny.lizard.chat

import android.graphics.Rect
import gg.destiny.lizard.base.extensions.dp

data class Emote(val key: String, val x: Int, val y: Int, val width: Int, val height: Int) {
  val drawable by lazy { EmoteDrawable(Rect(x.dp, y.dp, (x + width).dp, (y + height).dp), 16.dp) }
}

val pepoThink = Emote("PepoThink", -260, -30, 32, 32)
val sweatstiny = Emote("SWEATSTINY", 0, -209, 34, 30)

