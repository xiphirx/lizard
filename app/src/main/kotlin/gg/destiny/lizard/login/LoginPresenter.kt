package gg.destiny.lizard.login

import com.github.ajalt.timberkt.Timber.d
import gg.destiny.lizard.api.twitch.TwitchTvOAuth2Client
import gg.destiny.lizard.base.mvi.BasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler


class LoginPresenter(
    private val twitchTvOAuth2Client: TwitchTvOAuth2Client = TwitchTvOAuth2Client()
) : BasePresenter<LoginView, LoginModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<LoginModel> {
    val firstLoad = intent { it.firstLoad() }.flatMap { Observable.just(LoginModel.Welcome) }
    val requestTwitchLogin = intent { it.twitchLoginClicks }
        .flatMap {
          Observable.just(
              LoginModel.RequestOAuthLogin(
                  twitchTvOAuth2Client.authorizeUrl, twitchTvOAuth2Client.redirectSlug))
        }

    val oauthRedirect = intent { it.oauthRedirectUrl }
        .flatMap {
          d { "redirect $it"}
          Observable.just(LoginModel.OAuthRedirectLoading)
        }

    return Observable.merge(firstLoad, requestTwitchLogin, oauthRedirect)
  }
}
