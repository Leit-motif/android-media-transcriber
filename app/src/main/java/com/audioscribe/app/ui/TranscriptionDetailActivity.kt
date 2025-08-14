package com.audioscribe.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.audioscribe.app.ui.theme.AudioscribeTheme

class TranscriptionDetailActivity : ComponentActivity() {

	companion object {
		const val EXTRA_TRANSCRIPTION_TEXT = "extra_transcription_text"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val text = intent.getStringExtra(EXTRA_TRANSCRIPTION_TEXT) ?: ""
		setContent {
			AudioscribeTheme {
				TranscriptionDetailScreen(
					text = text,
					onBack = { finish() }
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranscriptionDetailScreen(text: String, onBack: () -> Unit) {
	val context = LocalContext.current
	var searchActive by remember { mutableStateOf(false) }
	var searchField by remember { mutableStateOf(TextFieldValue("")) }
	val query = searchField.text

	val highlightColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
	val annotated: AnnotatedString = remember(text, query) {
		if (query.isBlank()) return@remember AnnotatedString(text)
		val builder = AnnotatedString.Builder()
		var idx = 0
		val q = query
		while (idx < text.length) {
			val found = text.indexOf(q, idx, ignoreCase = true)
			if (found == -1) {
				builder.append(text.substring(idx))
				break
			}
			if (found > idx) builder.append(text.substring(idx, found))
			builder.pushStyle(SpanStyle(background = highlightColor))
			builder.append(text.substring(found, found + q.length))
			builder.pop()
			idx = found + q.length
		}
		builder.toAnnotatedString()
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					if (searchActive) {
                    TextField(
							value = searchField,
							onValueChange = { searchField = it },
							modifier = Modifier.fillMaxWidth(),
							singleLine = true,
                        placeholder = { Text("Search…") }
						)
					} else {
						Text("Transcription")
					}
				},
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
					}
				},
				actions = {
					IconButton(onClick = { searchActive = !searchActive; if (!searchActive) searchField = TextFieldValue("") }) {
						Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
					}
					IconButton(onClick = {
						val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
						cm.setPrimaryClip(ClipData.newPlainText("Transcription", text))
						Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
					}) {
						Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy")
					}
					IconButton(onClick = {
						val share = Intent(Intent.ACTION_SEND).apply {
							type = "text/plain"
							putExtra(Intent.EXTRA_TEXT, text)
						}
						context.startActivity(Intent.createChooser(share, "Share transcription"))
					}) {
						Icon(imageVector = Icons.Filled.Share, contentDescription = "Share")
					}
					IconButton(onClick = {
						val send = Intent(Intent.ACTION_SEND).apply {
							type = "text/plain"
							putExtra(Intent.EXTRA_TEXT, text)
						}
						try {
							// Try direct to ChatGPT if installed
							send.`package` = "com.openai.chatgpt"
							context.startActivity(send)
						} catch (_: Exception) {
							context.startActivity(Intent.createChooser(send, "Send to ChatGPT"))
						}
					}) {
						Icon(imageVector = Icons.Filled.Send, contentDescription = "Send to ChatGPT")
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
		) {
			if (text.isBlank()) {
				Text("No transcription available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
			} else {
				val scroll = rememberScrollState()
				Card(
					modifier = Modifier.fillMaxSize(),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
				) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(scroll)
					) {
						SelectionContainer {
							Text(
								text = annotated,
								style = MaterialTheme.typography.bodyMedium,
								modifier = Modifier.padding(16.dp),
								color = MaterialTheme.colorScheme.onSurface
							)
						}
					}
				}
			}
		}
	}
}


