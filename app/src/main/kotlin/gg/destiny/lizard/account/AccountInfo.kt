package gg.destiny.lizard.account

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import gg.destiny.lizard.core.api.SessionInformation
import gg.destiny.lizard.core.logging.L

enum class AccountSubscriptionTier {
  NONE,
  ONE,
  TWO,
  THREE,
  FOUR;

  companion object {
    fun of(value: String?): AccountSubscriptionTier {
      // TODO: Figure out the appropriate values here
      return NONE
    }
  }

  class TypeAdapter {
    @FromJson
    fun fromJson(json: String): AccountSubscriptionTier {
      L { "received $json" }
      return valueOf(json)
    }

    @ToJson
    fun toJson(tier: AccountSubscriptionTier): String {
      return tier.name
    }
  }
}

enum class AccountFeature(val key: String, val priority: Int, val color: Int? = null) {
  UNKNOWN("", -1),
  CONTRIBUTOR("flair5", 0),
  SUBSCRIBER("subscriber", 1, 0xFF488CE7.toInt()),
  SUBSCRIBER_TWITCH("flair9", 2, 0xFF488CE7.toInt()),
  SUBSCRIBER_T1("flair13", 3, 0xFF488CE7.toInt()),
  SUBSCRIBER_T2("flair1", 4, 0xFF488CE7.toInt()),
  SUBSCRIBER_T3("flair3", 5, 0xFF0060FF.toInt()),
  SUBSCRIBER_T4("flair8", 6, 0xFFA427D6.toInt()),
  BOT("bot", 7, 0xFFE79015.toInt()),
  VIP("vip", 8, 0xFF4DB524.toInt()),
  ADMIN("admin", 9, 0xFFB91010.toInt());

  companion object {
    private val VALUES = values()

    fun of(value: String): AccountFeature {
      VALUES.forEach {
        if (it.key == value) {
          return it
        }
      }
      return UNKNOWN
    }
  }
}

enum class AccountRole(val key: String) {
  USER("user"),
  UNKNOWN("");

  companion object {
    private val VALUES = values()

    fun of(value: String): AccountRole {
      VALUES.forEach {
        if (it.key == value) {
          return it
        }
      }
      return UNKNOWN
    }
  }
}

data class AccountInfo(
    val username: String,
    val nick: String,
    val userId: String,
    val email: String,
    val roles: List<AccountRole>,
    val features: List<AccountFeature>,
    val subscriptionTier: AccountSubscriptionTier
) {
  companion object {
    fun of(session: SessionInformation.Available): AccountInfo {
      return AccountInfo(
          username = session.username,
          nick = session.nick,
          userId = session.userId,
          email = session.email,
          roles = session.roles.map { AccountRole.of(it) },
          features = session.features.map { AccountFeature.of(it) },
          subscriptionTier = AccountSubscriptionTier.of(session.subscription)
      )
    }
  }
}
