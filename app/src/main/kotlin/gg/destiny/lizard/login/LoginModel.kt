package gg.destiny.lizard.login

data class LoginModel(
    val loginAuthorizeUrl: String? = null,
    val loginRedirectSlug: String? = null,
    val isLoading: Boolean = false,
    val error: LoginError? = null
)

sealed class PartialState {
  object Welcome : PartialState()
  data class Request(val authorizeUrl: String, val redirectKey: String) : PartialState()
  object Loading : PartialState()
  data class Error(val error: LoginError) : PartialState()
}

sealed class LoginError {
  object NoInternet : LoginError()
  data class Auth(val message: String) : LoginError()
  data class Http(val code: Int) : LoginError()
  object Unknown : LoginError()
}

