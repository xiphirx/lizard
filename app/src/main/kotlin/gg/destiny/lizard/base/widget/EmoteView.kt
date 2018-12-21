package gg.destiny.lizard.base.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import gg.destiny.lizard.base.extensions.scope
import gg.destiny.lizard.core.chat.Emote

class EmoteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private val density = context.resources.displayMetrics.density.toInt()
  var emote: Emote? = null
    set(value) {
      val prevValue = field
      field = value
      if (prevValue != value) {
        requestLayout()
      }
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val w = (emote?.w ?: 0) * density
    val h = (emote?.h ?: 0) * density
    setMeasuredDimension(w + paddingLeft + paddingRight, h + paddingTop + paddingBottom)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    emote?.let {
      canvas.scope {
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
//        EmoteDrawable.emote = emote
//        EmoteDrawable.draw(canvas)
      }
    }
  }
}
