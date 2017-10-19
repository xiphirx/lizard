package gg.destiny.lizard.account

enum class SubscriptionTier {
  NONE,
  ONE,
  TWO,
  THREE,
  FOUR;
}

data class AccountInfo(val name: String, val subscriptionTier: SubscriptionTier)
