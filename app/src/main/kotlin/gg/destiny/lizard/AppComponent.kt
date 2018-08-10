package gg.destiny.lizard

import dagger.Component
import gg.destiny.lizard.core.chat.ChatModule
import gg.destiny.lizard.core.network.NetworkModule
import gg.destiny.lizard.drawer.DrawerController
import gg.destiny.lizard.settings.SettingsController
import gg.destiny.lizard.stream.StreamController
import gg.destiny.lizard.stream.StreamPresenter
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ChatModule::class, NetworkModule::class])
interface AppComponent {
  fun inject(victim: StreamPresenter)
  fun inject(victim: DrawerController)
  fun inject(victim: SettingsController)
  fun inject(victim: StreamController)
}
