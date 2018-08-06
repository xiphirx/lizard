package gg.destiny.lizard.account

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import gg.destiny.lizard.App

class AccountInfoStorage(
    private val preferences: SharedPreferences =
        App.get().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE),
    moshi: Moshi = App.moshi
) {
  companion object {
    const val PREF_NAME = "account_storage"
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
