package com.audioscribe.app.data.database

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object DatabaseKeyStore {
	private const val PREFS_NAME = "db_secure_prefs"
	private const val KEY_DB_PASSPHRASE = "db_passphrase"

	private fun prefs(context: Context) = EncryptedSharedPreferences.create(
		context,
		PREFS_NAME,
		MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	fun getOrCreatePassphrase(context: Context): ByteArray {
		val existing = prefs(context).getString(KEY_DB_PASSPHRASE, null)
		if (existing != null) return android.util.Base64.decode(existing, android.util.Base64.NO_WRAP)
		val random = SecureRandom()
		val key = ByteArray(32) // 256-bit key
		random.nextBytes(key)
		prefs(context).edit().putString(KEY_DB_PASSPHRASE, android.util.Base64.encodeToString(key, android.util.Base64.NO_WRAP)).apply()
		return key
	}
}
