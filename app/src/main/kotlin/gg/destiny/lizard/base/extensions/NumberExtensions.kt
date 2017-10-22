package gg.destiny.lizard.base.extensions

import android.content.Context
import android.util.TypedValue
import gg.destiny.lizard.App

fun Int.dp(context: Context) =
  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics))

val Int.dp: Int
  get() = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), App.INSTANCE.resources.displayMetrics))
