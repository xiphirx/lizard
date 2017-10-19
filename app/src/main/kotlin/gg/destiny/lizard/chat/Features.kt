package gg.destiny.lizard.chat

import java.util.HashMap

data class Feature(val name: String, val color: Int, val priority: Int)

private val featureMap = HashMap<String, Feature>().apply {
  var priority = 0
  // Subscriber Tiers
  put("subscriber", Feature("Subscriber", 0xFF488CE7.toInt(), priority++))
  put("flair9", Feature("Twitch Subscriber", 0xFF488CE7.toInt(), priority++))
  put("flair13", Feature("Subscriber (T1)", 0xFF488CE7.toInt(), priority++))
  put("flair1", Feature("Subscriber (T2)", 0xFF488CE7.toInt(), priority++))
  put("flair3", Feature("Subscriber (T3)", 0xFF0060FF.toInt(), priority++))
  put("flair8", Feature("Subscriber (T4)", 0xFFA427D6.toInt(), priority++))

  // Special people
  put("vip", Feature("VIP", 0xFF4DB524.toInt(), priority++))
  put("admin", Feature("Admin", 0xFFB91010.toInt(), priority++))
}

fun featureOf(key: String) = featureMap[key]
