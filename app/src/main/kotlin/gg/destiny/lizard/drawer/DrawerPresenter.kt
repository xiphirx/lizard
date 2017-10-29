package gg.destiny.lizard.drawer

import com.github.ajalt.timberkt.Timber.d
import gg.destiny.lizard.App
import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.account.AccountManager
import gg.destiny.lizard.account.SubscriptionTier
import gg.destiny.lizard.api.DestinyApi
import gg.destiny.lizard.base.mvi.BasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DrawerPresenter(
    private val accountManager: AccountManager = App.accountManager,
    private val destinyApi: DestinyApi = DestinyApi()
) : BasePresenter<DrawerView, DrawerModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<DrawerModel> {
    val accountStatus = intent { it.firstLoad() }
        .flatMap {
          accountManager.isLoggedIn()
              .map {
                d { "yo wtf $it" }
                if (it) {
                  LoginStatus.LoggedIn(AccountInfo("Xiphirx", SubscriptionTier.FOUR))
                } else {
                  LoginStatus.LoggedOut
                }
              }
        }

    val requestTwitchLogin = intent { it.twitchLoginClicks }
        .flatMap {
          destinyApi.initiateLogin(DestinyApi.LoginService.TWITCH)
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
          destinyApi.completeLogin(it)
              // TODO: Figure out errors that are sent back
              .map {
                if (it.isSuccessful) {
                  LoginStatus.LoggedOut
                } else {
                  LoginStatus.Error(LoginError.Http(it.code()))
                }
              }
              .startWith(LoginStatus.Loading)
              .subscribeOn(Schedulers.io())
        }

    return Observable.merge(accountStatus, requestTwitchLogin, oauthRedirect)
        .observeOn(scheduler)
        .scan(DrawerModel(), { _, state -> reduce(state) })
  }

  private fun reduce(state: LoginStatus) = DrawerModel(state)
}
