package gg.destiny.lizard.login

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import gg.destiny.lizard.R
import gg.destiny.lizard.api.twitch.TwitchTvOOAuth2Client
import gg.destiny.lizard.base.controller.BaseController
import gg.destiny.lizard.base.extensions.tintCompoundDrawables
import gg.destiny.lizard.base.mvi.BaseView
import kotlinx.android.synthetic.main.controller_login.view.login_twitchtv

sealed class LoginModel {
  object Welcome : LoginModel()
}

interface LoginView : BaseView<LoginModel>

class LoginController : BaseController<LoginView, LoginModel, LoginPresenter>(), LoginView {
  private val oauth = TwitchTvOOAuth2Client()
  override fun createPresenter() = LoginPresenter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
    inflater.inflate(R.layout.controller_login, container, false).apply {
      with(login_twitchtv) {
        tintCompoundDrawables(ContextCompat.getColor(activity, R.color.twitchtv_secondary))
        setOnClickListener {
          LoginDialog(context, oauth.authorizeUrl()).show()
        }
      }
    }

  override fun render(model: LoginModel) {

  }
}
