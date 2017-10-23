package gg.destiny.lizard.api.twitch

import gg.destiny.lizard.api.oauth2.OAuth2Client
import gg.destiny.lizard.base.extensions.toHexString
import java.security.SecureRandom

class TwitchTvOAuth2Client(
    private val clientId: String = "hk3xmf0uwmsdhve6ylr4kjbp538pr6",
    override val redirectSlug: String = "gg.destiny.lizard://oauth.twitch",
    private val scopes: List<String> = listOf("user:edit")
) : OAuth2Client {

  private val secureRandom = SecureRandom()
  private var state: String = generateState()

  override val authorizeUrl: String
    get() {
      state = generateState()
      return ("${TwitchTvApi.BASE_URL}/kraken/oauth2/authorize?"
          + "client_id=$clientId"
          + "&redirect_uri=$redirectSlug"
          + "&scope=${scopes.joinToString(separator = "%20")}"
          + "&response_type=token"
          + "&state=$state")
    }

  override fun parseRedirection(redirectUrl: String): String {
    val pairs = redirectUrl.substring(redirectSlug.length + 1)
        .split("&")
        .map { it.split("=") }
        .filter { it.size == 2 }
        .map { it[0] to it[1] }
    var hasError = false
    var errorDescription = ""
    var verifiedState = false
    var accessToken = ""
    for ((key, value) in pairs) {
      when (key) {
        "state" -> verifiedState = value == state
        "error" -> hasError = true
        "error_description" -> errorDescription = value
        "access_token" -> accessToken = value
      }
    }

    if (hasError) {
      throw OAuth2Client.AuthError(errorDescription)
    }

    if (!verifiedState) {
      throw OAuth2Client.AuthError("Invalid state")
    }

    if (accessToken.isBlank()) {
      throw OAuth2Client.AuthError("No auth token received")
    }

    return accessToken
  }

  private fun generateState(): String {
    val bytes = ByteArray(32)
    secureRandom.nextBytes(bytes)
    return bytes.toHexString()
  }
}
