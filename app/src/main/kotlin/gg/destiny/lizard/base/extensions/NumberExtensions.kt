package gg.destiny.lizard.base.extensions

import android.content.Context
import android.util.TypedValue

fun Int.dp(context: Context) =
  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics))

