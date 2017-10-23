package gg.destiny.lizard.login

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class OAuthLoginDialog(
    context: Context,
    private val authUrl: String,
    private val redirectSlug: String,
    private val redirectListener: (String) -> Unit
) : Dialog(context) {
  private val overrideWebViewClient = object : WebViewClient() {
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
      super.onPageStarted(view, url, favicon)
      checkForRedirect(url)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      val url = request.url.toString()
      checkForRedirect(url)
      view.loadUrl(url)
      return true
    }

    private fun checkForRedirect(url: String) {
      if (url.startsWith(redirectSlug)) {
        redirectListener(url)
      }
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
