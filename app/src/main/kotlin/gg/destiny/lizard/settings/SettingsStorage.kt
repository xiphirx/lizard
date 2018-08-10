package gg.destiny.lizard.settings

import android.content.SharedPreferences
import gg.destiny.lizard.BuildConfig
import gg.destiny.lizard.core.settings.BooleanSetting
import gg.destiny.lizard.core.settings.SettingSpec
import gg.destiny.lizard.core.settings.StaticTextSetting
import javax.inject.Inject
import javax.inject.Named

class SettingsStorage @Inject constructor(
    @Named("settings") private val preferences: SharedPreferences
) {
  fun loadExistingSettings(): List<SettingSpec<out Any>> = listOf(
      BooleanSetting.DARK_MODE.load(preferences),
      StaticTextSetting.VERSION.apply { value = BuildConfig.VERSION_NAME }
  )

  fun updateBooleanSetting(setting: SettingSpec<Boolean>) {
    preferences.edit().putBoolean(setting.key, setting.value).apply()
  }

  private fun SettingSpec<Boolean>.load(preferences: SharedPreferences): SettingSpec<Boolean> {
    value = preferences.getBoolean(key, defaultValue)
    return this
  }
}
