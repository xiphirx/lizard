package gg.destiny.lizard.stream

import gg.destiny.lizard.App
import gg.destiny.lizard.base.mvi.BasePresenter
import gg.destiny.lizard.chat.Chat
import gg.destiny.lizard.api.twitch.TwitchTvApi
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class StreamPresenter(private val twitchTvApi: TwitchTvApi = App.TWITCH_TV,
                      private val chat: Chat = Chat()) :
    BasePresenter<StreamView, StreamModel>() {
  override fun bindIntents(scheduler: Scheduler) {
    val streamInfoObservable = intent { it.firstLoad() }
        .flatMap {
          twitchTvApi.getStreamInformation("destiny")
              .map {
                val stream = it.stream
                if (stream != null) {
                  StreamModel.Online(
                      title = stream.channel.status,
                      viewerCount = stream.viewers,
                      chatMessages = chat.messages(),
                      url = "https://player.twitch.tv/?channel=destiny")
                } else {
                  StreamModel.Offline(chat.messages())
                }
              }
              .startWith(StreamModel.Loading)
              .subscribeOn(Schedulers.io())
        }

    view(streamInfoObservable.observeOn(scheduler))
  }

  override fun attachView(view: StreamView) {
    super.attachView(view)
    chat.connect()
  }

  override fun detachView(retainInstance: Boolean) {
    super.detachView(retainInstance)
    if (!retainInstance) {
      chat.disconnect()
    }
  }
}
