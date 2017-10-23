package gg.destiny.lizard.api.oauth2

interface OAuth2Client {
  interface RedirectListener {
    fun onRedirect(url: String)
  }

  val authorizeUrl: String
  val redirectSlug: String
}
