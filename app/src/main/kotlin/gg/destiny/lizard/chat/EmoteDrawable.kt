package gg.destiny.lizard.chat

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import gg.destiny.lizard.base.extensions.scope
import gg.destiny.lizard.core.chat.Emote

object EmoteDrawable : Drawable() {
  private val paint = Paint()
  var density: Float = 1f
  var texture: Bitmap? = null
      set(value) {
        field?.recycle()
        field = value
      }

  var emote: Emote? = null
      set(value) {
        if (value != null) {
          bounds.set(0, 0, (dpToPx(value.w) - 0.5f).toInt(), (dpToPx(value.h) - 0.5f).toInt())
        }
        field = value
      }

  init {
    bounds = Rect()
  }

  override fun getIntrinsicWidth() = bounds.width()

  override fun getIntrinsicHeight() = bounds.height()

  override fun draw(canvas: Canvas) {
    val bm = texture ?: return
    val em = emote ?: return
    canvas.clipRect(0, 0, bounds.width(), bounds.height())
    canvas.scope {
      translate(dpToPx(em.x), dpToPx(em.y))
      canvas.drawBitmap(bm, 0f, 0f, paint)
    }
  }

  override fun setAlpha(alpha: Int) {
    // No-op
  }

  override fun getOpacity() = PixelFormat.TRANSLUCENT

  override fun setColorFilter(colorFilter: ColorFilter?) {
    // No-op
  }

  private fun dpToPx(dp: Int) = dp * density
}
