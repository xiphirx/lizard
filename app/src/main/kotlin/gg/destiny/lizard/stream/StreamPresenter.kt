package gg.destiny.lizard.stream

import com.github.ajalt.timberkt.d
import gg.destiny.lizard.App
import gg.destiny.lizard.account.AccountManager
import gg.destiny.lizard.api.TwitchTvApi
import gg.destiny.lizard.base.mvi.BasePresenter
import gg.destiny.lizard.chat.Chat
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class StreamPresenter(
    private val streamKey: String = "destiny",
    private val twitchTvApi: TwitchTvApi = App.twitchTv,
    private val chat: Chat = Chat(),
    private val accountManager: AccountManager = AccountManager()
) : BasePresenter<StreamView, StreamModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<StreamModel> {
    val streamInformation = intent { Observable.just(true) }
        .flatMap {
          twitchTvApi.getStreamInformation(streamKey)
              .map {
                val stream = it.stream
                if (stream != null) {
                  StreamStatus.Online(
                      title = stream.channel.status,
                      viewerCount = stream.viewers,
                      chatMessages = chat.messages(),
                      url = "https://player.twitch.tv/?channel=$streamKey&html5")
                } else {
                  StreamStatus.Offline(chat.messages())
                }
              }
              .startWith(StreamStatus.Loading)
              .subscribeOn(Schedulers.io())
        }

    val chatParticipationStatus = intent { accountManager.accountInfo() }
        .map { ChatParticipationStatus.Online(it) }
        .subscribeOn(Schedulers.io())

    return Observable.merge(streamInformation, chatParticipationStatus)
        .observeOn(scheduler)
        .scan(StreamModel(), { prev, state -> reduce(prev, state) })
  }

  private fun reduce(previousModel: StreamModel, partialState: Any): StreamModel {
    if (partialState is StreamStatus) {
      return previousModel.copy(streamStatus = partialState)
    }
    if (partialState is ChatParticipationStatus) {
      d { "Reducing $partialState"}
      return previousModel.copy(chatParticipationStatus = partialState)
    }
    return previousModel
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
