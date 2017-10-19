package gg.destiny.lizard.chat

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import gg.destiny.lizard.App
import gg.destiny.lizard.base.extensions.scope

class EmoteDrawable(val emoteBounds: Rect, val lineHeight: Int) : Drawable() {
  private val paint = Paint()

  init {
    setBounds(0, 0, emoteBounds.width(), emoteBounds.height())
  }

  override fun getIntrinsicWidth() = emoteBounds.width()

  override fun getIntrinsicHeight() = emoteBounds.height()

  override fun draw(canvas: Canvas) {
    canvas.clipRect(0, 0, emoteBounds.width(), emoteBounds.height())
    canvas.scope {
      translate(emoteBounds.left.toFloat(), emoteBounds.top.toFloat())
      canvas.drawBitmap(App.EMOTE_SHEET, 0f, 0f, paint)
    }
  }

  override fun setAlpha(alpha: Int) {
  }

  override fun getOpacity() = PixelFormat.TRANSLUCENT

  override fun setColorFilter(colorFilter: ColorFilter?) {
  }
}
