package gg.destiny.lizard.account

import com.jakewharton.rxrelay2.BehaviorRelay
import gg.destiny.lizard.api.DestinyApi
import io.reactivex.Observable
import javax.inject.Inject

class AccountManager @Inject constructor(
    private val destinyApi: DestinyApi,
    private val accountInfoStorage: AccountInfoStorage
) {
  private val accountInfoRelay = BehaviorRelay.create<AccountInfo>()

  init {
    accountInfoStorage.getCachedAccountInfo()?.let {
      accountInfoRelay.accept(it)
    }
  }

  /**
   * Initiates an OAuth login with the service provided.
   * @see [DestinyApi.initiateLogin]
   */
  fun initiateOAuthLogin(service: DestinyApi.LoginService) = destinyApi.initiateLogin(service)

  /**
   * Completes a previously initiated OAuth login
   * @see [DestinyApi.completeLogin]
   */
  fun completeOAuthLogin(redirectUrl: String) = destinyApi.completeLogin(redirectUrl)

  /** Retrieves the user's current account information from the server */
  fun queryAccountInfo() = destinyApi.getSessionInformation()

  /** Returns a hot observable of the user's cached account information */
  fun accountInfo(): Observable<AccountInfo> = accountInfoRelay

  /** Stores the user's account information to disk for later / offline usage */
  fun storeAccountInfo(accountInfo: AccountInfo) {
    accountInfoRelay.accept(accountInfo)
    accountInfoStorage.cacheAccountInfo(accountInfo)
  }

  /** Clears previously cached account information for the current user */
  fun clearAccountInfo() {
    accountInfoStorage.clearAccountInfo()
  }
}
