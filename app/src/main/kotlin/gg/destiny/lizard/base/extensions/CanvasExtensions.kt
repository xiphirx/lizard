package gg.destiny.lizard.base.extensions

import android.graphics.Canvas

fun Canvas.scope(block: Canvas.() -> Unit) {
  val saveCount = save()
  block.invoke(this)
  restoreToCount(saveCount)
}
