package gg.destiny.lizard.chat

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatStorage
import gg.destiny.lizard.core.chat.Emote
import gg.destiny.lizard.core.logging.L

class SharedPreferencesChatStorage(
    private val preferences: SharedPreferences,
    moshi: Moshi
) : ChatStorage {
  companion object {
    const val KEY_VERSION = "version"
    const val KEY_EMOTES = "emotes"
  }

  private val emoteTypeAdapter = moshi.adapter(Emote::class.java)

  override fun storeGuiPackageInfo(guiPackage: ChatGuiPackage) {
    synchronized(this) {
      L { "storing shit " }
      L { guiPackage.toString() }
      preferences.edit()
          .putString(KEY_VERSION, guiPackage.version)
          .putStringSet(
              KEY_EMOTES,
              guiPackage.emoteMap.map { emoteTypeAdapter.toJson(it.value) }.toSet())
          .apply()
    }
  }

  override fun getGuiPackageInfo(): ChatGuiPackage? {
    synchronized(this) {
      val version = preferences.getString(KEY_VERSION, null) ?: return null
      val emoteSet = preferences.getStringSet(KEY_EMOTES, null) ?: return null
      val emoteMap =
          emoteSet.mapNotNull { emoteTypeAdapter.fromJson(it) }.associate { it.name to it }
      return ChatGuiPackage(version, emoteMap)
    }
  }
}
