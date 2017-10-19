package gg.destiny.lizard.base.extensions

import android.util.TypedValue
import gg.destiny.lizard.App

val Int.dp: Int
  get() = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), App.INSTANCE.resources.displayMetrics))
