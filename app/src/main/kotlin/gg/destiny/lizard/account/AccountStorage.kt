package gg.destiny.lizard.account

data class AccountAccessToken(val token: String, val expiration: Long)

interface AccountStorage {
  fun getAccessToken(): AccountAccessToken?

  fun setAccessToken(accessToken: AccountAccessToken)
}
