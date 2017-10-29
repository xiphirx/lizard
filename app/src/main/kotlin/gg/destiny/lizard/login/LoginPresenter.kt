package gg.destiny.lizard.login

import gg.destiny.lizard.App
import gg.destiny.lizard.api.DestinyApi
import gg.destiny.lizard.api.SharedPreferencesCookieJar
import gg.destiny.lizard.base.mvi.BasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class LoginPresenter(
    private val destinyApi: DestinyApi = DestinyApi(App.okHttp, SharedPreferencesCookieJar())
) : BasePresenter<LoginView, LoginModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<LoginModel> {
    val firstLoad = intent { it.firstLoad() }.flatMap { Observable.just(PartialState.Welcome) }
    val requestTwitchLogin = intent { it.twitchLoginClicks }
        .flatMap {
          destinyApi.initiateLogin(DestinyApi.LoginService.TWITCH)
              .map {
                val url = it.raw().request().url()
                when (url.host()) {
                  DestinyApi.HOST -> PartialState.Welcome
                  else ->
                    PartialState.Request(
                        url.toString(), DestinyApi.oauthRedirectUri(DestinyApi.LoginService.TWITCH))
                }
              }
              .startWith(PartialState.Loading)
              .subscribeOn(Schedulers.io())
        }

    val oauthRedirect = intent { it.oauthRedirectUrl }
        .flatMap {
          destinyApi.completeLogin(it)
              // TODO: Figure out errors that are sent back
              .map {
                if (it.isSuccessful) {
                  PartialState.Welcome
                } else {
                  PartialState.Error(LoginError.Http(it.code()))
                }
              }
              .startWith(PartialState.Loading)
              .subscribeOn(Schedulers.io())
        }

    return Observable.merge(firstLoad, requestTwitchLogin, oauthRedirect)
        .observeOn(scheduler)
        .scan(LoginModel(), { _, state -> reduce(state) })
  }

  private fun reduce(state: PartialState) =
    when (state) {
      is PartialState.Welcome -> LoginModel()
      is PartialState.Request -> LoginModel(
          loginAuthorizeUrl = state.authorizeUrl, loginRedirectSlug = state.redirectKey)
      is PartialState.Loading -> LoginModel(isLoading = true)
      is PartialState.Error -> LoginModel(error = state.error)
    }
}
