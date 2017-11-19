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
          BiFunction<ChatGuiPackage, ChatGuiApi.PackageInfo, Pair<String, String>> {
            t1, t2 ->  t1.version to t2.version
          })
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.io())
          .filter { (currentVersion, newVersion) -> currentVersion != newVersion }
          .flatMap({ guiApi.getEmoteList() }, { (_, version), emotes -> version to emotes })
          .flatMap({ downloadEmoteTexture(it.first) }, { emotes, texture -> emotes to texture })
          .map { (emoteList, texturePath) ->
            val (version, emotes) = emoteList
            ChatGuiPackage(version, texturePath, emotes.associate { it.name to it })
          }
          .subscribe(
              { storage.storeGuiPackageInfo(it) ; updateDisposable = null },
              { e -> L(e) { "Couldn't update gui package info" } })
    }
  }

  private fun downloadEmoteTexture(version: String): Observable<String> {
    return guiApi.getEmoteTexture()
        .map {
          val textureFile = storage.getEmoteTextureFile(version)
          Okio.buffer(Okio.source(it.byteStream())).use { buffer ->
            Okio.sink(textureFile).use { sink ->
              buffer.readAll(sink)
            }
          }
          textureFile.absolutePath
        }
  }

  fun guiPackageInfo(): Observable<ChatGuiPackage> = chatGuiPackageRelay

  fun connect() {
    socket.connect()
  }

  fun disconnect() {
    socket.disconnect()
  }

  fun chatMessages() = socket.messages()

  fun sendMessage(message: String) {
    socket.sendMessage(message)
  }
}
