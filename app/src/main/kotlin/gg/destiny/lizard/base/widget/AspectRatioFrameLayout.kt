package gg.destiny.lizard.base.widget

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.widget.FrameLayout
import gg.destiny.lizard.R
import gg.destiny.lizard.base.extensions.useStyledAttributes

class AspectRatioFrameLayout : FrameLayout {
  companion object {
    private const val DEFAULT_RATIO = 1.0f
  }

  private var ratio = DEFAULT_RATIO

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
      super(context, attrs, defStyleAttr) {
    init(context, attrs)
  }

  constructor(context: Context,
              attrs: AttributeSet?,
              @AttrRes defStyleAttr: Int,
              @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    init(context, attrs)
  }

  fun init(context: Context, attrs: AttributeSet?) {
    attrs ?: return
    context.useStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout) {
      ratio = it.getFloat(R.styleable.AspectRatioFrameLayout_ratio, DEFAULT_RATIO)
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
    val heightSpec = MeasureSpec.makeMeasureSpec(
        (measuredWidth / ratio).toInt(), MeasureSpec.EXACTLY)
    super.onMeasure(widthSpec, heightSpec)
  }
}
