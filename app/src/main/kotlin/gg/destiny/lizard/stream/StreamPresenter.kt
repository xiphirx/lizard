package gg.destiny.lizard.stream

import gg.destiny.lizard.account.AccountManager
import gg.destiny.lizard.api.TwitchTvApi
import gg.destiny.lizard.base.mvi.BasePresenter
import gg.destiny.lizard.core.chat.Chat
import gg.destiny.lizard.core.chat.ChatGuiPackage
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

class StreamPresenter @Inject constructor(
    @Named("stream-key") private val streamKey: String,
    private val twitchTvApi: TwitchTvApi,
    private val chat: Chat,
    private val accountManager: AccountManager
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

    intent { Observable.just(true) }
        .doOnNext { chat.updateGuiPackageInfo() }
        .subscribeOn(Schedulers.io())
        .subscribe()

    val chatGuiPackageUpdates = intent { chat.guiPackageInfo() }
        .subscribeOn(Schedulers.io())

    val authoredChatMessages = intent { it.authoredChatMessages }
        .filter { it.isNotBlank() }
        .doOnNext { chat.sendMessage(it) }
        .subscribeOn(Schedulers.io())

    return Observable.merge(
        streamInformation, chatParticipationStatus, authoredChatMessages, chatGuiPackageUpdates)
        .observeOn(scheduler)
        .scan(StreamModel(), { prev, state -> reduce(prev, state) })
  }

  private fun reduce(previousModel: StreamModel, partialState: Any): StreamModel {
    return when (partialState) {
      is StreamStatus -> previousModel.copy(streamStatus = partialState)
      is ChatParticipationStatus -> previousModel.copy(chatParticipationStatus = partialState)
      is ChatGuiPackage -> previousModel.copy(chatGuiPackage = partialState)
      else -> previousModel
    }
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
