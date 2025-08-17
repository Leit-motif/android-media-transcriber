package com.audioscribe.app.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PromptStore {
	private const val PREFS_NAME = "secure_prefs" // reuse same encrypted prefs file
	private const val KEY_DEFAULT_PROMPT = "default_analysis_prompt"

	private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
		context,
		PREFS_NAME,
		MasterKey.Builder(context)
			.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
			.build(),
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)

	private val RECOMMENDED_DEFAULT: String = """
Please perform a comprehensive analysis of the following transcript.

Return the output in structured Markdown with clear section headings:

- Summary: A concise overview (5–7 sentences) capturing context and outcomes.
- Key Takeaways: Bullet points of the most important insights.
- Action Items: Specific, actionable steps with suggested owners and timeframes when inferable.
- Themes & Topics: High-level themes and recurring ideas (group related points together).
- Sentiment & Tone: Overall sentiment, notable shifts, and intensity.
- Risks, Gaps, Contradictions: Inconsistencies, uncertainties, or missing context to clarify.
- Follow-up Questions: Questions the listener should ask next to move forward.
- Entities: People, organizations, places, products, or key terms mentioned.
- References: If timestamps are present, reference them where helpful.

Guidelines:
- Be accurate and avoid speculation; note uncertainty explicitly.
- Use concise quotes only when necessary to support points.
- Prefer clarity and usefulness over verbosity.
""".trimIndent()

	fun saveDefaultPrompt(context: Context, prompt: String) {
		getPrefs(context).edit().putString(KEY_DEFAULT_PROMPT, prompt.trim()).apply()
	}

	fun getDefaultPrompt(context: Context): String {
		return getPrefs(context).getString(KEY_DEFAULT_PROMPT, RECOMMENDED_DEFAULT).orEmpty()
	}
}
