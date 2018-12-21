package gg.destiny.lizard.chat

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import android.text.style.UpdateAppearance
import android.util.Property
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import gg.destiny.lizard.GlideApp
import gg.destiny.lizard.base.extensions.dp
import gg.destiny.lizard.base.extensions.scope
import gg.destiny.lizard.core.chat.Emote

class EmoteSpan(context: Context, val emote: Emote, val invalidateListener: (() -> Unit)? = null) : DynamicDrawableSpan(), UpdateAppearance {
  companion object {
    const val lR = 0.213f
    const val lG = 0.715f
    const val lB = 0.072f
    const val sA = 0.143f
    const val sB = 0.140f
    const val sC = -0.283f
    private val workBounds = Rect()
  }

  private val colorMatrix = ColorMatrix()
  private val emoteBounds = Rect(0, 0, emote.w.dp(context), emote.h.dp(context))
  private var emoteDrawable: Drawable = BoundsDrawable().apply { bounds = emoteBounds }
  val glideTarget = object : SimpleTarget<Drawable>() {
    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
      resource.bounds.set(emoteBounds)
      emoteDrawable = resource
      invalidateListener?.invoke()
    }
  }

  var rotation: Float = 0f
  var translateX: Float = 0f
  var hueShift: Float = 0f

  object RotateProperty : Property<EmoteSpan, Float>(Float::class.java, "EMOTE_SPAN_ROTATE") {
    override fun get(span: EmoteSpan) = span.rotation

    override fun set(span: EmoteSpan, amount: Float) {
      span.rotation = amount
    }
  }

  object TranslateXProperty : Property<EmoteSpan, Float>(Float::class.java, "EMOTE_SPAN_TRANSLATE_X") {
    override fun get(span: EmoteSpan) = span.translateX

    override fun set(span: EmoteSpan, amount: Float) {
      span.translateX = amount
    }
  }

  object HueShiftProperty : Property<EmoteSpan, Float>(Float::class.java, "EMOTE_SPAN_HUE_SHIFT") {
    override fun get(span: EmoteSpan) = span.hueShift

    override fun set(span: EmoteSpan, amount: Float) {
      span.hueShift = amount
    }
  }

  override fun getDrawable() = emoteDrawable

  override fun getSize(
      paint: Paint,
      text: CharSequence,
      start: Int,
      end: Int,
      fm: Paint.FontMetricsInt?
  ): Int {
    if (fm != null) {
      val paintFm = paint.fontMetricsInt
      fm.ascent = paintFm.ascent
      fm.descent = paintFm.descent
      fm.top = paintFm.top
      fm.bottom = paintFm.bottom
    }

    return emoteBounds.width()
  }

  fun setVisible(context: Context, status: Boolean) {
    if (status) {
      GlideApp.with(context)
          .load(emote.url)
          .diskCacheStrategy(DiskCacheStrategy.DATA)
          .into(glideTarget)
    } else {
      Glide.with(context).clear(glideTarget)
    }
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
    if (drawable is BoundsDrawable) return
    canvas.scope {
      val lineHeight = bottom - top
      val newY = top + (lineHeight / 2f) - (drawable.bounds.height() / 2f)
      val clipBounds = canvas.clipBounds
      workBounds.set(drawable.bounds)
      workBounds.offset(x.toInt(), newY.toInt())
      clipBounds.union(workBounds)
      canvas.clipRect(clipBounds, Region.Op.REPLACE)
      canvas.translate(x + translateX, newY)
      canvas.rotate(rotation)
      if (hueShift == 0f) {
        drawable.colorFilter = null
      } else {
        hueShift(colorMatrix, hueShift)
        drawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
      }
      drawable.draw(canvas)
    }
  }

  private fun hueShift(colorMatrix: ColorMatrix, degrees: Float) {
    colorMatrix.reset()
    val radians = degrees * Math.PI / 180.0
    val cs = Math.cos(radians).toFloat()
    val sn = Math.sin(radians).toFloat()
    colorMatrix.set(floatArrayOf(
        lR + cs * (1 - lR) + sn * -lR, lG + cs * -lG + sn * -lG, lB + cs * -lB + sn * (1 - lB), 0f, 0f,
        lR + cs * -lR + sn * sA, lG + cs * (1 - lG) + sn * sB, lB + cs * -lB + sn * sC, 0f, 0f,
        lR + cs * -lR + sn * -(1-lR), lG + cs * -lG + sn * lG, lB + cs * (1 - lB) + sn * lB, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
  }

  private class BoundsDrawable : Drawable() {
    override fun draw(canvas: Canvas?) {}
    override fun setAlpha(alpha: Int) {}
    override fun getOpacity(): Int {
      return PixelFormat.OPAQUE
    }
    override fun setColorFilter(colorFilter: ColorFilter?) {}
  }
}
