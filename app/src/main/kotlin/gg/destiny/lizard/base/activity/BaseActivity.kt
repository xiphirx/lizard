package gg.destiny.lizard.base.activity

import android.content.res.Resources
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatActivity
import gg.destiny.lizard.R

abstract class BaseActivity : AppCompatActivity() {
  protected var darkTheme = true

  override fun onApplyThemeResource(theme: Resources.Theme, resid: Int, first: Boolean) {
    applyTheme(theme)
    super.onApplyThemeResource(theme, resid, first)
  }

  @StyleRes
  protected open val darkThemeStyle = R.style.BaseTheme_Material_Dark

  @StyleRes
  protected open val lightThemeStyle = R.style.BaseTheme_Material_Light

  protected open fun applyTheme(theme: Resources.Theme) {
    theme.applyStyle(if (darkTheme) darkThemeStyle else lightThemeStyle, true)
    theme.applyStyle(R.style.Default, true)
  }
}
