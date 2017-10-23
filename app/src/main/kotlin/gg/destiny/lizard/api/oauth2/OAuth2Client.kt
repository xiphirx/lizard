package gg.destiny.lizard.api.oauth2

interface OAuth2Client {
  interface RedirectListener {
    fun onRedirect(url: String)
  }

  val authorizeUrl: String
  val redirectSlug: String

  /** Parses the given redirection and returns the access token, or throws an exception */
  fun parseRedirection(redirectUrl: String): String

  class AuthError(val reason: String) : RuntimeException()
}
