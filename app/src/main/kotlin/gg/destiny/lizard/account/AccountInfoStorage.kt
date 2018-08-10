package gg.destiny.lizard.account

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Named

class AccountInfoStorage @Inject constructor(
    @Named("account-info") private val preferences: SharedPreferences,
    moshi: Moshi
) {
  companion object {
    const val KEY_ACCOUNT_INFO = "account-info"

    const val MISSING = "missing"
  }

  private val accountInfoTypeAdapter = moshi.adapter(AccountInfo::class.java)

  fun getCachedAccountInfo(): AccountInfo? {
    val json = preferences.getString(KEY_ACCOUNT_INFO, MISSING)
    if (json == MISSING) {
      return null
    }
    return accountInfoTypeAdapter.fromJson(json)
  }

  fun cacheAccountInfo(accountInfo: AccountInfo) {
    val json = accountInfoTypeAdapter.toJson(accountInfo)
    preferences.edit().putString(KEY_ACCOUNT_INFO, json).apply()
  }

  fun clearAccountInfo() {
    preferences.edit().remove(KEY_ACCOUNT_INFO).apply()
  }
}
