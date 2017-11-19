package gg.destiny.lizard.chat

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Region
import android.text.style.DynamicDrawableSpan
import gg.destiny.lizard.base.extensions.scope
import gg.destiny.lizard.core.chat.Emote

class EmoteSpan(private val emote: Emote) : DynamicDrawableSpan() {
  private val bounds: Rect = Rect()

  override fun getDrawable() = EmoteDrawable

  override fun getSize(
      paint: Paint,
      text: CharSequence,
      start: Int,
      end: Int,
      fm: Paint.FontMetricsInt?
  ): Int {
    drawable.emote = emote
    val rect = drawable.bounds

    if (fm != null) {
      val paintFm = paint.fontMetricsInt
      fm.ascent = paintFm.ascent
      fm.descent = paintFm.descent
      fm.top = paintFm.top
      fm.bottom = paintFm.bottom
    }

    return rect.width()
  }

  override fun draw(
      canvas: Canvas,
      text: CharSequence,
      start: Int,
      end: Int,
      x: Float,
      top: Int,
      y: Int,
      bottom: Int,
      paint: Paint) {
    canvas.scope {
      drawable.emote = emote
      val lineHeight = bottom - top
      val newY = top + (lineHeight / 2f - drawable.intrinsicHeight / 2f)
      val clipBounds = canvas.clipBounds
      bounds.set(drawable.bounds)
      bounds.offset(x.toInt(), newY.toInt())
      clipBounds.union(bounds)
      canvas.clipRect(clipBounds, Region.Op.REPLACE)
      canvas.translate(x, newY)
      drawable.draw(canvas)
    }
  }
}
