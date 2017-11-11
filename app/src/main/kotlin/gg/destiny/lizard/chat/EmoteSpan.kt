package gg.destiny.lizard.chat

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import gg.destiny.lizard.base.extensions.scope

class EmoteSpan(private val drawable: Drawable) : DynamicDrawableSpan() {
  private val bounds: Rect = Rect()

  override fun getDrawable() = drawable

  override fun getSize(
      paint: Paint,
      text: CharSequence,
      start: Int,
      end: Int,
      fm: Paint.FontMetricsInt?
  ): Int {
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
