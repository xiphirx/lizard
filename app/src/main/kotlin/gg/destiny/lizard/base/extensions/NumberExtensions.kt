package gg.destiny.lizard.base.extensions

import android.content.Context
import android.util.TypedValue

annotation class Dp

fun @receiver:Dp Int.dp(context: Context) =
  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics))

