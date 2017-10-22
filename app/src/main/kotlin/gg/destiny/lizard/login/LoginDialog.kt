package gg.destiny.lizard.login

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class LoginDialog(context: Context, private val authUrl: String) : Dialog(context) {
  private val overrideWebViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      view.loadUrl(request.url.toString())
      return true
    }
  }

  private val webView = WebView(context).apply {
    settings.domStorageEnabled = true
    settings.javaScriptEnabled = true
    webViewClient = overrideWebViewClient
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(webView)
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
  }

  override fun show() {
    super.show()
    webView.loadUrl(authUrl)
  }

  override fun dismiss() {
    super.dismiss()
    webView.destroy()
  }
}
