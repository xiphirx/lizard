package gg.destiny.lizard.core.settings

interface SettingSpec<T> {
  var value: T
  val key: String
  val defaultValue: T
}

enum class BooleanSetting : SettingSpec<Boolean> {
  DARK_MODE {
    override val key = "dark_mode"
    override val defaultValue = true
    override var value = defaultValue
  }
}

enum class StaticTextSetting : SettingSpec<String> {
  VERSION {
    override val key = "version"
    override var value = ""
    override var defaultValue = ""
  }
}

data class SettingsModel(val settings: List<SettingSpec<out Any>> = emptyList())
