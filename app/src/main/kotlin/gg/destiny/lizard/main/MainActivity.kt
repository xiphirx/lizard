package gg.destiny.lizard.main

import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import gg.destiny.lizard.R
import gg.destiny.lizard.account.AccountInfo
import gg.destiny.lizard.account.SubscriptionTier
import gg.destiny.lizard.base.activity.BaseActivity
import gg.destiny.lizard.login.LoginController
import gg.destiny.lizard.stream.StreamController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.controller_stream.*
import kotlinx.android.synthetic.main.item_account_info.view.*

class MainActivity : BaseActivity() {
  lateinit var router: Router
  lateinit var drawerLayout: DrawerLayout
  lateinit var drawerToggle: ActionBarDrawerToggle

  private val drawerAdapter = FlexAdapter<Any>().apply {
    register<AccountInfo>(R.layout.item_account_info) { info, view, _ ->
      with(view) {
        account_info_name.text = info.name
        account_info_subscription_tier.text = info.subscriptionTier.name
      }
    }

    items.add(AccountInfo("Dgg User", SubscriptionTier.FOUR))
  }

  override fun onCreate(savedInstance: Bundle?) {
    super.onCreate(savedInstance)
    setContentView(R.layout.activity_main)
    router = Conductor.attachRouter(this, content_frame, savedInstance)
    if (!router.hasRootController()) {
      router.setRoot(RouterTransaction.with(StreamController()))
    }

    drawerLayout = drawer_layout
    drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.password_toggle_content_description, R.string.abc_action_bar_home_description)
    with(drawer_recycler) {
      adapter = drawerAdapter
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
  }

  override fun onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed()
    }
  }
}
