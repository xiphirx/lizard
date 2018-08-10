package gg.destiny.lizard.drawer

import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.account.AccountManager
import gg.destiny.lizard.api.DestinyApi
import gg.destiny.lizard.base.mvi.BasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class DrawerPresenter @Inject constructor(
    private val accountManager: AccountManager
) : BasePresenter<DrawerView, DrawerModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<DrawerModel> {
    val accountStatus = intent { it.firstLoad() }
        .flatMap {
          retrieveLoginStatus().startWith(LoginStatus.Loading).subscribeOn(Schedulers.io())
        }

    val requestTwitchLogin = intent { it.twitchLoginClicks }
        .flatMap {
          accountManager.initiateOAuthLogin(DestinyApi.LoginService.TWITCH)
              .map {
                val url = it.raw().request().url()
                when (url.host()) {
                  DestinyApi.HOST -> LoginStatus.LoggedOut
                  else ->
                    LoginStatus.Request(
                        url.toString(), DestinyApi.oauthRedirectUri(DestinyApi.LoginService.TWITCH))
                }
              }
              .startWith(LoginStatus.Loading)
              .subscribeOn(Schedulers.io())
        }

    val oauthRedirect = intent { it.oauthRedirectUrl }
        .flatMap {
          accountManager.completeOAuthLogin(it)
              // TODO: Figure out errors that are sent back
              .flatMap {
                if (it.isSuccessful) {
                  retrieveLoginStatus()
                } else {
                  Observable.just(LoginStatus.Error(LoginError.Http(it.code())))
                }
              }
              .startWith(LoginStatus.Loading)
              .subscribeOn(Schedulers.io())
        }

    return Observable.merge(accountStatus, requestTwitchLogin, oauthRedirect)
        .observeOn(scheduler)
        .scan(DrawerModel()) { _, state -> reduce(state) }
  }

  private fun retrieveLoginStatus(): Observable<LoginStatus> {
    return accountManager.queryAccountInfo()
        .map {
          if (it.isSuccessful) {
            it.body()?.let {
              val accountInfo = AccountInfo.of(it)
              accountManager.storeAccountInfo(accountInfo)
              LoginStatus.LoggedIn(accountInfo)
            } ?: LoginStatus.LoggedOut
          } else {
            accountManager.clearAccountInfo()
            LoginStatus.LoggedOut
          }
        }
  }

  private fun reduce(state: LoginStatus) = DrawerModel(state)
}
