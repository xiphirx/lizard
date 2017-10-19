package gg.destiny.lizard.base.extensions

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.StringRes
import android.support.annotation.StyleableRes
import android.util.AttributeSet

fun Context.string(@StringRes id: Int): String = getString(id)
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
