package gg.destiny.lizard.main

import android.os.Bundle
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import gg.destiny.lizard.R
import gg.destiny.lizard.base.activity.BaseActivity
import gg.destiny.lizard.drawer.DrawerController
import gg.destiny.lizard.stream.StreamController
import kotlinx.android.synthetic.main.activity_main.drawer_content_frame
import kotlinx.android.synthetic.main.activity_main.stream_content_frame

class MainActivity : BaseActivity() {
  lateinit var streamRouter: Router
  lateinit var drawerRouter: Router

  override fun onCreate(savedInstance: Bundle?) {
    super.onCreate(savedInstance)
    setContentView(R.layout.activity_main)
    streamRouter = Conductor.attachRouter(this, stream_content_frame, savedInstance)
    if (!streamRouter.hasRootController()) {
      streamRouter.setRoot(RouterTransaction.with(StreamController()))
    }

    drawerRouter = Conductor.attachRouter(this, drawer_content_frame, savedInstance)
    if (!drawerRouter.hasRootController()) {
      drawerRouter.setRoot(RouterTransaction.with(DrawerController()))
    }
  }

  override fun onBackPressed() {
    if (!drawerRouter.handleBack() && !streamRouter.handleBack()) {
      super.onBackPressed()
    }
  }
}
