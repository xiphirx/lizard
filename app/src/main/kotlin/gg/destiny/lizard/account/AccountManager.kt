package gg.destiny.lizard.account

class AccountManager(private val accountStorage: AccountStorage) {
  fun isLoggedIn() = accountStorage.getAccessToken() != null
}
