package gg.destiny.lizard.core.chat

import com.jakewharton.rxrelay2.BehaviorRelay
import gg.destiny.lizard.core.logging.L
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import okio.Okio

class Chat(
    private val socket: ChatSocket,
    private val guiApi: ChatGuiApi,
    private val storage: ChatStorage
) {
  private val chatGuiPackageRelay = BehaviorRelay.create<ChatGuiPackage>()

  init {
    Observable.defer { Observable.just(storage.getGuiPackageInfo() ?: emptyPackage) }
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe(chatGuiPackageRelay)
  }

  private var updateDisposable: Disposable? = null

  fun updateGuiPackageInfo() {
    synchronized(this) {
      if (updateDisposable != null) {
        return
      }
      updateDisposable = Observable.zip(
          chatGuiPackageRelay,
          guiApi.getPackageInfo(),
          BiFunction<ChatGuiPackage, ChatGuiApi.PackageInfo, Pair<ChatGuiPackage, String>> {
            t1, t2 ->  t1 to t2.version
          })
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.io())
          .filter { (currentPackage, newVersion) ->
            currentPackage.emoteMap.isEmpty() || currentPackage.version != newVersion
          }
          .flatMap({ guiApi.getEmoteList(it.second) }, { (_, version), emotes -> version to emotes })
          .map { (version, emoteList) -> ChatGuiPackage(version, emoteList.associate { it.name to it }) }
          .subscribe({
            storage.storeGuiPackageInfo(it)
            chatGuiPackageRelay.accept(it)
            updateDisposable = null
          }, { e -> L(e) { "Couldn't update gui package info" } })
    }
  }

  fun guiPackageInfo(): Observable<ChatGuiPackage> = chatGuiPackageRelay

  fun connect() {
    socket.connect()
  }

  fun disconnect() {
    socket.disconnect()
  }

  fun messages() = socket.messages()

  fun sendMessage(message: String) {
    socket.sendMessage(message)
  }
}
