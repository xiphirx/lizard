package gg.destiny.lizard.chat

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import gg.destiny.lizard.App
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatStorage
import gg.destiny.lizard.core.chat.Emote
import gg.destiny.lizard.core.logging.L
import java.io.File

class SharedPreferencesChatStorage(
    private val preferences: SharedPreferences =
        App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE),
    private val storageDirectory: File = App.get().getDir(PREF_NAME, Context.MODE_PRIVATE),
    moshi: Moshi = App.moshi
) : ChatStorage {
  companion object {
    const val PREF_NAME = "chat_gui_storage"
    const val KEY_VERSION = "version"
    const val KEY_TEXTURE_PATH = "texture-path"
    const val KEY_EMOTES = "emotes"
  }

  private val emoteTypeAdapter = moshi.adapter(Emote::class.java)

  override fun storeGuiPackageInfo(guiPackage: ChatGuiPackage) {
    synchronized(this) {
      L { "storing shit " }
      L { guiPackage.toString() }
      preferences.edit()
          .putString(KEY_VERSION, guiPackage.version)
          .putString(KEY_TEXTURE_PATH, guiPackage.texturePath)
          .putStringSet(
              KEY_EMOTES,
              guiPackage.emoteMap.map { emoteTypeAdapter.toJson(it.value) }.toSet())
          .apply()
    }
  }

  override fun getGuiPackageInfo(): ChatGuiPackage? {
    synchronized(this) {
      val version = preferences.getString(KEY_VERSION, null) ?: return null
      val texturePath = preferences.getString(KEY_TEXTURE_PATH, null) ?: return null
      val emoteSet = preferences.getStringSet(KEY_EMOTES, null) ?: return null
      val emoteMap =
          emoteSet.mapNotNull { emoteTypeAdapter.fromJson(it) }.associate { it.name to it }
      return ChatGuiPackage(version, texturePath, emoteMap)
    }
  }

  override fun getEmoteTextureFile(version: String) = File(storageDirectory, "emotes-$version.png")
}
