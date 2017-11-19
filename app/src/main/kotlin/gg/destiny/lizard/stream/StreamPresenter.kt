package gg.destiny.lizard.stream

import android.graphics.BitmapFactory
import gg.destiny.lizard.App
import gg.destiny.lizard.account.AccountCookieJar
import gg.destiny.lizard.account.AccountManager
import gg.destiny.lizard.api.TwitchTvApi
import gg.destiny.lizard.base.mvi.BasePresenter
import gg.destiny.lizard.chat.AppChatStorage
import gg.destiny.lizard.chat.EmoteDrawable
import gg.destiny.lizard.core.chat.Chat
import gg.destiny.lizard.core.chat.ChatGuiApi
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatSocket
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class StreamPresenter(
    private val streamKey: String = "destiny",
    private val twitchTvApi: TwitchTvApi = App.twitchTv,
    private val chat: Chat = Chat(
        socket = ChatSocket(
            okHttpClient = App.okHttp, moshi = App.moshi, cookieJar = AccountCookieJar()),
        guiApi = ChatGuiApi(okHttpClient = App.okHttp, moshi = App.moshi),
        storage = AppChatStorage()),
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
                      chatMessages = chat.chatMessages(),
                      url = "https://player.twitch.tv/?channel=$streamKey&html5")
                } else {
                  StreamStatus.Offline(chat.chatMessages())
                }
              }
              .startWith(StreamStatus.Loading)
              .subscribeOn(Schedulers.io())
        }

    val chatParticipationStatus = intent { accountManager.accountInfo() }
        .map { ChatParticipationStatus.Online(it) }
        .subscribeOn(Schedulers.io())

    val chatGuiUpdate = intent { Observable.just(true) }
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
    if (partialState is StreamStatus) {
      return previousModel.copy(streamStatus = partialState)
    }
    if (partialState is ChatParticipationStatus) {
      return previousModel.copy(chatParticipationStatus = partialState)
    }
    if (partialState is ChatGuiPackage) {
      return previousModel.copy(chatGuiPackage = partialState)
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
