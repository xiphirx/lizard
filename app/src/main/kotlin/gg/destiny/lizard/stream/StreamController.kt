package gg.destiny.lizard.stream

import android.graphics.BitmapFactory
import android.support.annotation.StringRes
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.e
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxrelay2.PublishRelay
import gg.destiny.lizard.App
import gg.destiny.lizard.R
import gg.destiny.lizard.base.controller.BaseController
import gg.destiny.lizard.base.mvi.BaseView
import gg.destiny.lizard.base.widget.EmoteView
import gg.destiny.lizard.chat.ComboMessage
import gg.destiny.lizard.chat.EmoteDrawable
import gg.destiny.lizard.chat.EmoteSpan
import gg.destiny.lizard.chat.createChatAdapter
import gg.destiny.lizard.core.chat.ChatGuiPackage
import gg.destiny.lizard.core.chat.ChatSocket
import gg.destiny.lizard.core.chat.Emote
import gg.destiny.lizard.core.chat.emptyPackage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.controller_stream.view.*
import kotlinx.android.synthetic.main.item_chat_emote.view.chat_emote_image

interface StreamView : BaseView<StreamModel> {
  val authoredChatMessages: Observable<String>
}

class StreamController : BaseController<StreamView, StreamModel, StreamPresenter>(), StreamView {
  init {
    App.get().appComponent.inject(this)
  }

  private var chatMessageHash: Int = 0
  private var chatMessageSubscription: Disposable? = null
  private var chatUserCount: Int = 0
    set(value) {
      field = if (value < 0) 0 else value
    }

  override val authoredChatMessages: PublishRelay<String> = PublishRelay.create<String>()
  private val chatAdapter = createChatAdapter({ chatGuiPackage }, { highlightNick })
  private var textureLoadingDisposable: Disposable? = null
  private var chatGuiPackage: ChatGuiPackage = emptyPackage
  private var highlightNick: String? = null
  private var lockAutoScroll = true
  private lateinit var chatTextEditText: TextInputEditText
  private lateinit var chatRecyclerView: RecyclerView
  private lateinit var chatLayoutManager: LinearLayoutManager
  private lateinit var chatEmoteRecyclerView: RecyclerView
  private lateinit var chatEmoteLayoutManager: GridLayoutManager

  private val chatEmoteClickListener = View.OnClickListener { v ->
    v as? EmoteView ?: return@OnClickListener
    val emote = v.emote ?: return@OnClickListener
    chatTextEditText.append(" ${emote.name}")
  }

  private val chatEmoteAdapter = FlexAdapter<Emote>().apply {
    register<Emote>(R.layout.item_chat_emote) { emote, view, _ ->
      view.chat_emote_image.emote = emote
      view.chat_emote_image.setOnClickListener(chatEmoteClickListener)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.controller_stream, container, false).apply {
      chatLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
      chatRecyclerView = stream_chat_recycler_view
      with(chatRecyclerView) {
        itemAnimator = null
        adapter = chatAdapter
        layoutManager = chatLayoutManager
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
          override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            lockAutoScroll = when (newState) {
              SCROLL_STATE_IDLE -> !chatRecyclerView.canScrollVertically(1)
              else -> false
            }
          }
        })
        viewTreeObserver.addOnPreDrawListener {
          if (lockAutoScroll) {
            chatRecyclerView.scrollToPosition(chatAdapter.items.lastIndex)
          }
          true
        }
      }

      chatEmoteLayoutManager = GridLayoutManager(context, 4)
      chatEmoteRecyclerView = stream_chat_emote_recycler_view.apply {
        adapter = chatEmoteAdapter
        layoutManager = chatEmoteLayoutManager
      }

      stream_chat_emote_button.setOnClickListener {
        stream_chat_emote_recycler_view.visibility =
            if (stream_chat_emote_recycler_view.visibility == View.GONE) {
              View.VISIBLE
            } else {
              View.GONE
            }
      }

      with(stream_web_view.settings) {
        javaScriptEnabled = true
        mediaPlaybackRequiresUserGesture = false
        domStorageEnabled = true
        allowContentAccess = true
        allowFileAccess = true
      }

      stream_web_view.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
          view.loadUrl(request.url.toString())
          return true
        }
      }

      chatTextEditText = stream_chat_edit_text
      RxTextView.editorActionEvents(chatTextEditText)
          .filter {
            it.actionId() == EditorInfo.IME_ACTION_SEND ||
                it.keyEvent()?.keyCode == KeyEvent.KEYCODE_ENTER
          }
          .doAfterNext { it.view().text = "" }
          .map { d { "sending ${it.view().text}" } ; it.view().text.toString() }
          .subscribe(authoredChatMessages)
    }
  }

  override fun render(model: StreamModel) {
    when (model.streamStatus) {
      is StreamStatus.Online -> {
        layout.stream_chat_offline_message.visibility = View.GONE
        layout.stream_video_container.visibility = View.VISIBLE
        layout.stream_viewer_num.text = "${model.streamStatus.viewerCount}"
        registerChatObservable(model.streamStatus.chatMessages)
        setupStream(model.streamStatus.url)
      }
      is StreamStatus.Offline -> {
        layout.stream_video_container.visibility = View.GONE
        layout.stream_chat_offline_message.visibility = View.VISIBLE
        registerChatObservable(model.streamStatus.chatMessages)
        setupStream(null)
      }
      is StreamStatus.Loading -> {
        setupStream(null)
      }
    }

    when (model.chatParticipationStatus) {
      is ChatParticipationStatus.Offline ->
          layout.stream_chat_text_input_layout.visibility = View.GONE
      is ChatParticipationStatus.Online -> {
        setChatCapabilities(editable = true, hint = R.string.chat_text_hint)
        highlightNick = model.chatParticipationStatus.accountInfo.nick
      }
      is ChatParticipationStatus.Banned ->
          setChatCapabilities(editable = false, hint = R.string.chat_text_hint_banned)
    }

    updateChatGuiPackage(model.chatGuiPackage)
  }

  private fun updateChatGuiPackage(chatGuiPackage: ChatGuiPackage) {
    if (this.chatGuiPackage == chatGuiPackage) {
      return
    }

    this.chatGuiPackage = chatGuiPackage

    if (chatGuiPackage.texturePath.isBlank()) {
      return
    }

    val densityDpi = layout.context.resources.displayMetrics.densityDpi
    textureLoadingDisposable?.dispose()
    textureLoadingDisposable = Observable.just(chatGuiPackage.texturePath)
        .map {
          val options = BitmapFactory.Options().apply {
            inScreenDensity = densityDpi
            inTargetDensity = densityDpi
            inDensity = DisplayMetrics.DENSITY_DEFAULT
          }
          BitmapFactory.decodeFile(chatGuiPackage.texturePath, options)
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { EmoteDrawable.texture = it }

    chatEmoteAdapter.items.clear()
    chatEmoteAdapter.items.addAll(chatGuiPackage.emoteMap.values)
  }

  private fun setChatCapabilities(editable: Boolean, @StringRes hint: Int) {
    with(layout.stream_chat_text_input_layout) {
      visibility = android.view.View.VISIBLE
      isEnabled = editable
    }
    with(layout.stream_chat_edit_text) {
      isEnabled = editable
      setHint(hint)
    }
  }

  private fun setupStream(url: String?) {
    val newUrl = url ?: "about:blank"
    view?.stream_web_view?.loadUrl(newUrl)
  }

  private fun registerChatObservable(chatMessages: Observable<ChatSocket.Message>) {
    if (chatMessageHash == chatMessages.hashCode()) {
      return
    }
    chatMessageHash = chatMessages.hashCode()
    chatMessageSubscription?.dispose()
    chatMessageSubscription = chatMessages
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { handleChatMessage(it) },
            { e(it) { "Error adding message" } })
  }

  private fun handleChatMessage(message: ChatSocket.Message) {
    when (message) {
      is ChatSocket.Message.Names -> {
        chatUserCount = message.connectioncount
        updateChatUserCount(chatUserCount)
        chatAdapter.items.add(message)
      }
      is ChatSocket.Message.UserMessage -> handleUserChatMessage(message)
      is ChatSocket.Message.Join -> updateChatUserCount(++chatUserCount)
      is ChatSocket.Message.Quit -> updateChatUserCount(--chatUserCount)
    }
  }

  private fun handleUserChatMessage(message: ChatSocket.Message.UserMessage) {
    val lastMessage = chatAdapter.items.lastOrNull()
    val trimmedMessage = message.data.trim()
    val comboIndex = chatAdapter.items.lastIndex
    if (lastMessage is ComboMessage) {
      when (trimmedMessage) {
        lastMessage.emoteSpan.emote.name -> {
          // Increment an ongoing combo
          lastMessage.count++
          lastMessage.ticked = true
        }
        else -> {
          // Complete a combo
          lastMessage.completed = true
          chatAdapter.items.add(message)
        }
      }
      chatAdapter.notifyItemChanged(comboIndex)
    } else if (lastMessage is ChatSocket.Message.UserMessage &&
        trimmedMessage == lastMessage.data.trim() &&
        trimmedMessage in chatGuiPackage.emoteMap) {
      // Begin a combo
      val emote = chatGuiPackage.emoteMap[trimmedMessage]!!
      chatAdapter.items[comboIndex] = ComboMessage(EmoteSpan(emote))
      chatAdapter.notifyItemChanged(comboIndex)
    } else {
      chatAdapter.items.add(message)
    }
  }

  private fun updateChatUserCount(count: Int) {
    view?.stream_chat_count?.text = "$count"
  }
}
