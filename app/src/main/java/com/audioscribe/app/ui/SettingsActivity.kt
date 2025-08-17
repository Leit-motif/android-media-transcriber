package com.audioscribe.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.audioscribe.app.ui.theme.AudioscribeTheme
import com.audioscribe.app.utils.ApiKeyStore
import com.audioscribe.app.utils.PromptStore
import com.audioscribe.app.utils.TranscriptionSettingsStore

class SettingsActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			AudioscribeTheme {
				SettingsScreen(onBack = { finish() })
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(onBack: () -> Unit) {
	val context = androidx.compose.ui.platform.LocalContext.current
	var apiKey by remember { mutableStateOf(ApiKeyStore.getApiKey(context)) }
	var defaultPrompt by remember { mutableStateOf(PromptStore.getDefaultPrompt(context)) }
	var speed by remember { mutableFloatStateOf(TranscriptionSettingsStore.getSpeedFactor(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(inner)
				.padding(16.dp)
				.verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text("OpenAI API Key", style = MaterialTheme.typography.titleMedium)
			OutlinedTextField(
				value = apiKey,
				onValueChange = { apiKey = it },
				modifier = Modifier.fillMaxWidth(),
				placeholder = { Text("sk-...") },
				visualTransformation = PasswordVisualTransformation()
			)
			Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				Button(onClick = {
					ApiKeyStore.saveApiKey(context, apiKey)
					Toast.makeText(context, "API key saved", Toast.LENGTH_SHORT).show()
				}) {
					Text("Save API Key")
				}
			}

			HorizontalDivider()

			Text("Default Analysis Prompt", style = MaterialTheme.typography.titleMedium)
			Text(
				"Used when sending transcripts to ChatGPT. You can customize this for your workflow.",
				style = MaterialTheme.typography.bodySmall
			)
			OutlinedTextField(
				value = defaultPrompt,
				onValueChange = { defaultPrompt = it },
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(min = 160.dp),
				placeholder = { Text("Enter your default instructions for analysis…") },
				maxLines = 12
			)
			Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
				Button(onClick = {
					PromptStore.saveDefaultPrompt(context, defaultPrompt)
					Toast.makeText(context, "Default prompt saved", Toast.LENGTH_SHORT).show()
				}) {
					Text("Save Prompt")
				}
			}

			HorizontalDivider()

			Text("Transcription Speed-Up", style = MaterialTheme.typography.titleMedium)
			Text(
				"Shorten audio to reduce API cost. Higher speeds may slightly reduce accuracy.",
				style = MaterialTheme.typography.bodySmall
			)
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				listOf(1.0f to "Off", 1.25f to "1.25x", 1.5f to "1.5x", 2.0f to "2.0x").forEach { (value, label) ->
					FilterChip(
						selected = speed == value,
						onClick = { speed = value; TranscriptionSettingsStore.saveSpeedFactor(context, value) },
						label = { Text(label) }
					)
				}
			}
		}
	}
}


