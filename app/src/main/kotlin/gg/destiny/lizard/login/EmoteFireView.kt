package gg.destiny.lizard.login

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import gg.destiny.lizard.base.extensions.scope
import gg.destiny.lizard.chat.EMOTE_MAP
import java.util.Random

class EmoteFireView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private class FloatyBoi(private val emote: Drawable, private val view: View) {
    companion object {
      private val random = Random()
    }

    private var x = 0f
    private var y = 0f
    private var alpha = 1f
    private var speedMultiplier = 1f
    private var initialBirth = false

    fun birth() {
      x = random.nextFloat() * (view.width - emote.bounds.width())
      y = (view.height + (random.nextInt(5) + 5) * emote.bounds.height()).toFloat()
      alpha = 1f
      emote.alpha = 255
      speedMultiplier = random.nextFloat() / 4f + 0.1f
    }

    fun update(dt: Float) {
      if (view.width * view.height == 0) {
        return
      }

      if (!initialBirth) {
        birth()
        initialBirth = true
      }

      y -= view.height * speedMultiplier * dt
      alpha -= speedMultiplier * dt
      emote.alpha = (255 * alpha).toInt()

      if (emote.alpha <= 0 || y < emote.bounds.height()) {
        birth()
      }
    }

    fun draw(canvas: Canvas) {
      if (view.width * view.height == 0) {
        return
      }

      canvas.scope {
        translate(x, y)
        emote.draw(canvas)
      }
    }
  }

  private val floatyBois = mutableListOf<FloatyBoi>()
  private val animator = TimeAnimator()
  private val animatorListener = TimeAnimator.TimeListener { _, _, dt ->
    for (floatyBoi in floatyBois) {
      floatyBoi.update(dt / 1000f)
    }
    postInvalidate()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    EMOTE_MAP.entries.forEach {
      floatyBois.add(FloatyBoi(it.value.drawable.mutate(), this))
    }
  }

  override fun onDraw(canvas: Canvas) {
    for (floatyBoi in floatyBois) {
      floatyBoi.draw(canvas)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    animator.start()
    animator.setTimeListener(animatorListener)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    animator.setTimeListener(null)
  }
}
