package gg.destiny.lizard.login

import com.github.ajalt.timberkt.Timber.d
import gg.destiny.lizard.api.oauth2.OAuth2Client
import gg.destiny.lizard.api.twitch.TwitchTvOAuth2Client
import gg.destiny.lizard.base.mvi.BasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler


class LoginPresenter(
    private val twitchTvOAuth2Client: TwitchTvOAuth2Client = TwitchTvOAuth2Client()
) : BasePresenter<LoginView, LoginModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<LoginModel> {
    val firstLoad = intent { it.firstLoad() }.flatMap { Observable.just(PartialState.Welcome) }
    val requestTwitchLogin = intent { it.twitchLoginClicks }
        .flatMap {
          Observable.just(
              PartialState.Request(
                  twitchTvOAuth2Client.authorizeUrl, twitchTvOAuth2Client.redirectSlug))
        }

    val oauthRedirect = intent { it.oauthRedirectUrl }
        .flatMap {
          d { "redirect $it"}
          if (it.startsWith(twitchTvOAuth2Client.redirectSlug)) {
            try {
              twitchTvOAuth2Client.parseRedirection(it)
            } catch (e: Exception) {
              return@flatMap Observable.just(
                  when (e) {
                    is OAuth2Client.AuthError -> PartialState.Error(LoginError.Auth(e.reason))
                    else -> PartialState.Error(LoginError.Unknown)
                  }
              )
            }
          }
          Observable.just(PartialState.Loading)
        }

    return Observable.merge(firstLoad, requestTwitchLogin, oauthRedirect)
        .scan(LoginModel(), { _, state -> reduce(state) })
  }

  private fun reduce(state: PartialState) =
    when (state) {
      is PartialState.Welcome -> LoginModel()
      is PartialState.Request -> LoginModel(
          loginAuthorizeUrl = state.authorizeUrl, loginRedirectSlug = state.redirectSlug)
      is PartialState.Loading -> LoginModel(isLoading = true)
      is PartialState.Error -> LoginModel(error = state.error)
    }
}
