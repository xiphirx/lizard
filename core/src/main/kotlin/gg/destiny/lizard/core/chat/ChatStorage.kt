package gg.destiny.lizard.core.chat

interface ChatStorage {
  fun storeGuiPackageInfo(guiPackage: ChatGuiPackage)
  fun getGuiPackageInfo(): ChatGuiPackage?
}
