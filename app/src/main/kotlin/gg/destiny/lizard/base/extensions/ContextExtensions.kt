package gg.destiny.lizard.base.extensions

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorRes
import android.support.annotation.StyleableRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet

fun Context.useStyledAttributes(attrs: AttributeSet,
                                @StyleableRes styleableRes: IntArray,
                                useBlock: (TypedArray) -> Unit) {
  val typedArray = obtainStyledAttributes(attrs, styleableRes)
  try {
    useBlock.invoke(typedArray)
  } finally {
    typedArray.recycle()
  }
}

fun Context.color(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)
