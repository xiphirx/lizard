package gg.destiny.lizard.stream

import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.ajalt.timberkt.e
import gg.destiny.lizard.R
import gg.destiny.lizard.base.controller.BaseController
import gg.destiny.lizard.base.mvi.BaseView
import gg.destiny.lizard.chat.ChatMessage
import gg.destiny.lizard.chat.createChatAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.controller_stream.view.stream_chat_count
import kotlinx.android.synthetic.main.controller_stream.view.stream_chat_edit_text
import kotlinx.android.synthetic.main.controller_stream.view.stream_chat_offline_message
import kotlinx.android.synthetic.main.controller_stream.view.stream_chat_recycler_view
import kotlinx.android.synthetic.main.controller_stream.view.stream_chat_text_input_layout
import kotlinx.android.synthetic.main.controller_stream.view.stream_video_container
import kotlinx.android.synthetic.main.controller_stream.view.stream_viewer_num
import kotlinx.android.synthetic.main.controller_stream.view.stream_web_view

interface StreamView : BaseView<StreamModel>

class StreamController : BaseController<StreamView, StreamModel, StreamPresenter>(), StreamView {
  private var chatMessageHash: Int = 0
  private var chatMessageSubscription: Disposable? = null
  private var chatUserCount: Int = 0
    set(value) {
      field = if (value < 0) 0 else value
    }

  private val chatAdapter = createChatAdapter()
  private lateinit var chatRecyclerView: RecyclerView

  override fun createPresenter() = StreamPresenter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.controller_stream, container, false).apply {
      chatRecyclerView = stream_chat_recycler_view
      with(chatRecyclerView) {
        adapter = chatAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
      is ChatParticipationStatus.Online ->
          setChatCapabilities(editable = true, hint = R.string.chat_text_hint)
      is ChatParticipationStatus.Banned ->
          setChatCapabilities(editable = false, hint = R.string.chat_text_hint_banned)
    }
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

  private fun registerChatObservable(chatMessages: Observable<ChatMessage>) {
    if (chatMessageHash != chatMessages.hashCode()) {
      chatMessageHash = chatMessages.hashCode()
      chatMessageSubscription?.dispose()
      chatMessageSubscription = chatMessages
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              { handleChatMessage(it) },
              { e(it) { "Error adding message" } })
    }
  }

  private fun handleChatMessage(message: ChatMessage) {
    when (message) {
      is ChatMessage.Names -> {
        chatUserCount = message.connectioncount
        updateChatUserCount(chatUserCount)
      }
      is ChatMessage.Message -> {
        val autoScroll = !chatRecyclerView.canScrollVertically(1)
        chatAdapter.items.add(message)
        if (autoScroll) {
          chatRecyclerView.scrollToPosition(chatAdapter.items.lastIndex)
        }
      }
      is ChatMessage.Join -> updateChatUserCount(++chatUserCount)
      is ChatMessage.Quit -> updateChatUserCount(--chatUserCount)
    }
  }

  private fun updateChatUserCount(count: Int) {
    view?.stream_chat_count?.text = "$count"
  }
}
