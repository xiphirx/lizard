package gg.destiny.lizard.drawer

import gg.destiny.lizard.account.AccountInfo

data class DrawerModel(val loginStatus: LoginStatus = LoginStatus.LoggedOut)

sealed class LoginStatus {
  object LoggedOut : LoginStatus()
  data class Request(val authorizeUrl: String, val redirectSlug: String) : LoginStatus()
  object Loading : LoginStatus()
  data class Error(val error: LoginError) : LoginStatus()
  data class LoggedIn(val accountInfo: AccountInfo) : LoginStatus()
}

sealed class LoginError {
  object NoInternet : LoginError()
  data class Auth(val message: String) : LoginError()
  data class Http(val code: Int) : LoginError()
  object Unknown : LoginError()
}

