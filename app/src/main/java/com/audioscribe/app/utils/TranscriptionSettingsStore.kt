package com.audioscribe.app.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TranscriptionSettingsStore {
	private const val PREFS_NAME = "secure_prefs"
	private const val KEY_SPEED_FACTOR = "transcription_speed_factor"
	private const val DEFAULT_SPEED = 1.0f

	private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
		context,
		PREFS_NAME,
		MasterKey.Builder(context)
			.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
			.build(),
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	fun getSpeedFactor(context: Context): Float {
		return getPrefs(context).getFloat(KEY_SPEED_FACTOR, DEFAULT_SPEED)
	}

	fun saveSpeedFactor(context: Context, value: Float) {
		getPrefs(context).edit().putFloat(KEY_SPEED_FACTOR, value).apply()
	}
}
