package gg.destiny.lizard.login

import android.app.Dialog
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import gg.destiny.lizard.R
import gg.destiny.lizard.base.controller.BaseController
import gg.destiny.lizard.base.extensions.color
import gg.destiny.lizard.base.extensions.tintCompoundDrawables
import gg.destiny.lizard.base.mvi.BaseView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_login.view.login_twitchtv
import kotlinx.android.synthetic.main.controller_login.view.login_view_flipper

interface LoginView : BaseView<LoginModel> {
  companion object {
    const val FLIPPER_LOGIN_BUTTONS_INDEX = 0
    const val FLIPPER_LOADING_INDEX = 1
  }
  val twitchLoginClicks: Observable<Any>
  val oauthRedirectUrl: Observable<String>
}

class LoginController : BaseController<LoginView, LoginModel, LoginPresenter>(), LoginView {
  override val twitchLoginClicks: Observable<Any> by lazy {
    RxView.clicks(layout.login_twitchtv)
  }

  override val oauthRedirectUrl: PublishRelay<String> = PublishRelay.create()

  private val redirectListener = { url: String -> oauthRedirectUrl.accept(url) }
  private var loginDialog: Dialog? = null

  override fun createPresenter() = LoginPresenter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
    inflater.inflate(R.layout.controller_login, container, false).apply {
      login_twitchtv.tintCompoundDrawables(context.color(R.color.twitchtv_secondary))
    }

  override fun render(model: LoginModel) {
    model.error?.let {
      showError(it)
      return
    }

    if (model.isLoading) {
      showLoading()
      return
    }

    if (model.loginAuthorizeUrl != null && model.loginRedirectSlug != null) {
      requestLogin(model.loginAuthorizeUrl, model.loginRedirectSlug)
      return
    }

    showWelcome()
  }

  private fun setDisplayedChild(index: Int) {
    layout.login_view_flipper.displayedChild = index
  }

  private fun requestLogin(authorizeUrl: String, redirectSlug: String) {
    loginDialog?.dismiss()
    loginDialog = OAuthLoginDialog(
        layout.context, authorizeUrl, redirectSlug, redirectListener).apply {
      show()
    }
  }

  private fun showWelcome() {
    setDisplayedChild(LoginView.FLIPPER_LOGIN_BUTTONS_INDEX)
  }

  private fun showLoading() {
    loginDialog?.dismiss()
    setDisplayedChild(LoginView.FLIPPER_LOADING_INDEX)
  }

  private fun showError(error: LoginError) {
    loginDialog?.dismiss()
    val context = layout.context
    AlertDialog.Builder(context)
        .setTitle(R.string.login_controller_error_title)
        .apply {
          when (error) {
            is LoginError.NoInternet ->
                setMessage(R.string.login_controller_error_no_internet)
            is LoginError.Http ->
                setMessage(
                    context.getString(R.string.login_controller_error_http_error, error.code))
            is LoginError.Auth ->
                setMessage(context.getString(R.string.login_controller_error_auth, error.message))
            is LoginError.Unknown -> setMessage(R.string.login_controller_error_unknown)
          }
        }
        .show()
  }
}
