package com.xbrighton.app

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureTokenStore {
    private const val PREFS_NAME = "xbrighton_secure_store"
    private const val KEY_EMAIL = "email"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(context: Context, email: String, accessToken: String, refreshToken: String) {
        getPrefs(context).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getEmail(context: Context): String? = getPrefs(context).getString(KEY_EMAIL, null)

    fun getAccessToken(context: Context): String? = getPrefs(context).getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(context: Context): String? = getPrefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun clearSession(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_EMAIL)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }
}
