package gg.destiny.lizard.login

sealed class LoginModel {
  object Welcome : LoginModel()

  data class RequestOAuthLogin(val authorizeUrl: String, val redirectSlug: String) : LoginModel()

  object OAuthRedirectLoading : LoginModel()
}
