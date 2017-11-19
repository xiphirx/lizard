package gg.destiny.lizard.core.chat

val emptyPackage = ChatGuiPackage("", "", mapOf())

data class ChatGuiPackage(
    val version: String,
    val texturePath: String,
    val emoteMap: Map<String, Emote>
)

data class Emote(val name: String, val x: Int, val y: Int, val w: Int, val h: Int)
