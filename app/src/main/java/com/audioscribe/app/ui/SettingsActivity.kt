package com.audioscribe.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.audioscribe.app.ui.theme.AudioscribeTheme
import com.audioscribe.app.utils.ApiKeyStore

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
				.padding(16.dp),
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
					onBack()
				}) {
					Text("Save")
				}
			}
		}
	}
}


