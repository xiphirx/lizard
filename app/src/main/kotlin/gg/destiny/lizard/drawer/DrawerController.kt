package gg.destiny.lizard.drawer

import android.app.Dialog
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import gg.destiny.lizard.R
import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.base.controller.BaseController
import gg.destiny.lizard.base.mvi.BaseView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_account_info.view.account_info_name
import kotlinx.android.synthetic.main.item_account_info.view.account_info_subscription_tier
import kotlinx.android.synthetic.main.item_login_nag.view.login_google
import kotlinx.android.synthetic.main.item_login_nag.view.login_nag_flipper
import kotlinx.android.synthetic.main.item_login_nag.view.login_reddit
import kotlinx.android.synthetic.main.item_login_nag.view.login_twitchtv
import kotlinx.android.synthetic.main.item_login_nag.view.login_twitter

interface DrawerView : BaseView<DrawerModel> {
  val twitchLoginClicks: Observable<Any>
  val redditLoginClicks: Observable<Any>
  val googleLoginClicks: Observable<Any>
  val twitterLoginClicks: Observable<Any>
  val oauthRedirectUrl: Observable<String>
}

data class LoginNag(var showLoading: Boolean = false)

class DrawerController : BaseController<DrawerView, DrawerModel, DrawerPresenter>(), DrawerView {
  override val twitchLoginClicks: Relay<Any> = PublishRelay.create()
  override val redditLoginClicks: Relay<Any> = PublishRelay.create()
  override val googleLoginClicks: Relay<Any> = PublishRelay.create()
  override val twitterLoginClicks: Relay<Any> = PublishRelay.create()
  override val oauthRedirectUrl: PublishRelay<String> = PublishRelay.create()

  private var clicksDisposable: CompositeDisposable? = null
  private val redirectListener = { url: String -> oauthRedirectUrl.accept(url) }
  private var loginDialog: Dialog? = null
  private val loginNag = LoginNag()

  private val drawerAdapter = FlexAdapter<Any>().apply {
    register<AccountInfo>(R.layout.item_account_info) { info, view, _ ->
      with(view) {
        account_info_name.text = info.nick
        account_info_subscription_tier.text = info.subscriptionTier.name
      }
    }

    register<LoginNag>(R.layout.item_login_nag) { loginNag, view, _ ->
      view.login_nag_flipper.displayedChild = if (loginNag.showLoading) 1 else 0
      clicksDisposable?.dispose()
      clicksDisposable = CompositeDisposable(
          RxView.clicks(view.login_twitchtv).subscribe(twitchLoginClicks),
          RxView.clicks(view.login_reddit).subscribe(redditLoginClicks),
          RxView.clicks(view.login_google).subscribe(googleLoginClicks),
          RxView.clicks(view.login_twitter).subscribe(twitterLoginClicks))
    }
  }

  override fun createPresenter() = DrawerPresenter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View =
      RecyclerView(container.context).apply {
        adapter = drawerAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
      }

  override fun render(model: DrawerModel) {
    when (model.loginStatus) {
      is LoginStatus.LoggedOut -> showLoginNag()
      is LoginStatus.Request ->
        requestLogin(model.loginStatus.authorizeUrl, model.loginStatus.redirectSlug)
      is LoginStatus.Loading -> showLoginLoading()
      is LoginStatus.Error -> showLoginError(model.loginStatus.error)
      is LoginStatus.LoggedIn -> showAccountInfo(model.loginStatus.accountInfo)
    }
  }

  private fun requestLogin(authorizeUrl: String, redirectSlug: String) {
    loginDialog?.dismiss()
    loginDialog = OAuthLoginDialog(
        layout.context, authorizeUrl, redirectSlug, redirectListener).apply {
      show()
      setOnCancelListener {
        showLoginNag(false)
      }
    }
  }

  private fun showAccountInfo(accountInfo: AccountInfo) {
    if (drawerAdapter.items.firstOrNull() !is AccountInfo) {
      drawerAdapter.items.setFirst(accountInfo)
    }
  }

  private fun showLoginNag(loading: Boolean = false) {
    loginNag.showLoading = loading
    if (drawerAdapter.items.firstOrNull() !is LoginNag) {
      drawerAdapter.items.setFirst(loginNag)
    } else {
      drawerAdapter.notifyItemChanged(0)
    }
  }

  private fun showLoginLoading() {
    showLoginNag(true)
    loginDialog?.dismiss()
  }

  private fun showLoginError(error: LoginError) {
    loginDialog?.dismiss()
    showLoginNag(false)
    val context = layout.context
    AlertDialog.Builder(context)
        .setTitle(R.string.login_error_title)
        .apply {
          when (error) {
            is LoginError.NoInternet ->
                setMessage(R.string.login_error_no_internet)
            is LoginError.Http ->
                setMessage(
                    context.getString(R.string.login_error_http_error, error.code))
            is LoginError.Auth ->
                setMessage(context.getString(R.string.login_error_auth, error.message))
            is LoginError.Unknown -> setMessage(R.string.login_error_unknown)
          }
        }
        .show()
  }

  private fun <E> MutableList<E>.setFirst(item: E) {
    if (isEmpty()) add(item) else set(0, item)
  }
}
