package com.audioscribe.app.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for the OpenAI API key using EncryptedSharedPreferences.
 */
object ApiKeyStore {

	private const val PREFS_NAME = "secure_prefs"
	private const val KEY_OPENAI = "openai_api_key"

	private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
		context,
		PREFS_NAME,
		MasterKey.Builder(context)
			.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
			.build(),
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	fun saveApiKey(context: Context, apiKey: String) {
		getPrefs(context).edit().putString(KEY_OPENAI, apiKey.trim()).apply()
	}

	fun getApiKey(context: Context): String {
		return getPrefs(context).getString(KEY_OPENAI, "").orEmpty()
	}

	fun isConfigured(context: Context): Boolean = getApiKey(context).isNotBlank()
}


