package gg.destiny.lizard.core.chat

import java.io.File

interface ChatStorage {
  fun storeGuiPackageInfo(guiPackage: ChatGuiPackage)

  fun getGuiPackageInfo(): ChatGuiPackage?

  fun getEmoteTextureFile(version: String): File
}
