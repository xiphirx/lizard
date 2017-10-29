package gg.destiny.lizard.account

import gg.destiny.lizard.App
import io.reactivex.Observable

class AccountManager(private val storage: AccountStorage = App.accountStorage) {
  fun isLoggedIn(): Observable<Boolean> = storage.sessionAvailable()
}
