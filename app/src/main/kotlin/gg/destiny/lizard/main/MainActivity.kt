package gg.destiny.lizard.main

import android.os.Bundle
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import gg.destiny.lizard.R
import gg.destiny.lizard.base.activity.BaseActivity
import gg.destiny.lizard.drawer.DrawerController
import gg.destiny.lizard.navigation.Navigator
import gg.destiny.lizard.settings.SettingsController
import gg.destiny.lizard.stream.StreamController
import kotlinx.android.synthetic.main.activity_main.drawer_content_frame
import kotlinx.android.synthetic.main.activity_main.drawer_layout
import kotlinx.android.synthetic.main.activity_main.stream_content_frame


class MainActivity : BaseActivity(), Navigator {
  private lateinit var streamRouter: Router
  private lateinit var drawerRouter: Router

  override fun onCreate(savedInstance: Bundle?) {
    super.onCreate(savedInstance)
    setContentView(R.layout.activity_main)
    streamRouter = Conductor.attachRouter(this, stream_content_frame, savedInstance)
    if (!streamRouter.hasRootController()) {
      streamRouter.setRoot(RouterTransaction.with(StreamController()))
    }

    drawerRouter = Conductor.attachRouter(this, drawer_content_frame, savedInstance)
    if (!drawerRouter.hasRootController()) {
      drawerRouter.setRoot(
          RouterTransaction.with(DrawerController().apply { navigator = this@MainActivity }))
    }

  }

  override fun onBackPressed() {
    if (drawer_layout.isDrawerOpen(drawer_content_frame)) {
      if (!drawerRouter.handleBack()) {
        drawer_layout.closeDrawers()
      }
    } else if (!streamRouter.handleBack()) {
      super.onBackPressed()
    }
  }

  override fun navigateToSettings() {
    drawer_layout.closeDrawers()
    streamRouter.pushController(RouterTransaction.with(SettingsController()))
  }
}
