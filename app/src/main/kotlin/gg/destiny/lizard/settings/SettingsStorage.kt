package gg.destiny.lizard.settings

import android.content.Context
import android.content.SharedPreferences
import gg.destiny.lizard.App
import gg.destiny.lizard.core.settings.BooleanSetting
import gg.destiny.lizard.core.settings.SettingSpec

class SettingsStorage(
    private val preferences: SharedPreferences =
        App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
) {
  companion object {
    private const val PREF_NAME = "app_settings"
  }

  fun loadExistingSettings(): List<SettingSpec<out Any>> = listOf(
      BooleanSetting.DARK_MODE.load(preferences)
  )

  fun updateBooleanSetting(setting: SettingSpec<Boolean>) {
    preferences.edit().putBoolean(setting.key, setting.value).apply()
  }

  private fun SettingSpec<Boolean>.load(preferences: SharedPreferences): SettingSpec<Boolean> {
    value = preferences.getBoolean(key, defaultValue)
    return this
  }
}
