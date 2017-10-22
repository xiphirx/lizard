package gg.destiny.lizard.base.extensions

import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.Button

fun Button.tintCompoundDrawables(@ColorInt color: Int) {
  post {
    // We post this to the handler because vector drawables dont seem to be available
    // immediately upon inflation
    compoundDrawables.filter { it != null }
        .forEach {
          DrawableCompat.wrap(it)
          DrawableCompat.setTint(it, color)
        }
  }
}
