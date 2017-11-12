package gg.destiny.lizard.drawer

import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.api.DestinyApi
import gg.destiny.lizard.base.mvi.BasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DrawerPresenter(
    private val destinyApi: DestinyApi = DestinyApi()
) : BasePresenter<DrawerView, DrawerModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<DrawerModel> {
    val accountStatus = intent { it.firstLoad() }
        .flatMap {
          destinyApi.getSessionInformation()
              .map {
                if (it.isSuccessful) {
                  it.body()?.let {
                    LoginStatus.LoggedIn(AccountInfo.of(it))
                  } ?: LoginStatus.LoggedOut
                } else {
                  LoginStatus.LoggedOut
                }
              }
              .startWith(LoginStatus.Loading)
              .subscribeOn(Schedulers.io())
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
                  // TODO: retrieve session info
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
