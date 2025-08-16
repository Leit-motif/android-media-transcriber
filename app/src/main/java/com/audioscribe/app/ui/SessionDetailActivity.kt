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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audioscribe.app.data.database.entity.TranscriptionSession
import com.audioscribe.app.data.database.entity.SessionStatus
import com.audioscribe.app.data.repository.SessionRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class SessionDetailActivity : ComponentActivity() {
    
    private lateinit var sessionRepository: SessionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionRepository = SessionRepository(this)
        
        val sessionId = intent.getLongExtra("session_id", -1L)
        if (sessionId == -1L) {
            finish()
            return
        }
        
        setContent {
            SessionDetailScreen(
                sessionId = sessionId,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetailScreen(
    sessionId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionRepository = remember { SessionRepository(context) }
    val scope = rememberCoroutineScope()
    
    var session by remember { mutableStateOf<TranscriptionSession?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Load session (one-shot)
    LaunchedEffect(sessionId) {
        try {
            isLoading = true
            session = sessionRepository.getSession(sessionId)
            if (session == null) error = "Session not found"
        } catch (e: Exception) {
            error = "Failed to load session: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    // Reactively collect chunks and build transcript text
    val chunksFlow = remember { sessionRepository.getChunksForSession(sessionId) }
    val chunks by chunksFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val transcriptText = remember(chunks) {
        chunks.filter { it.text.isNotBlank() }
            .sortedBy { it.chunkIndex }
            .joinToString(separator = "\n\n") { it.text }
    }
    
    // Delete session function
    fun deleteSession() {
        scope.launch {
            try {
                sessionRepository.deleteSession(sessionId)
                Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                onBack()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to delete session: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = session?.title ?: "Session Details",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
					// Always show delete button if session exists
					if (session != null) {
						IconButton(
							onClick = {
								showDeleteDialog = true
							}
						) {
							Icon(Icons.Default.Delete, contentDescription = "Delete Session")
						}
					}
					
					// Show other actions only when there's transcript text
					if (transcriptText.isNotEmpty()) {
						IconButton(
							onClick = {
								val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
								cm.setPrimaryClip(ClipData.newPlainText("Transcription", transcriptText))
								Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
							}
						) {
							Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
						}

						IconButton(
							onClick = {
								val share = Intent(Intent.ACTION_SEND).apply {
									type = "text/plain"
									putExtra(Intent.EXTRA_TEXT, transcriptText)
								}
								context.startActivity(Intent.createChooser(share, "Share transcription"))
							}
						) {
							Icon(Icons.Default.Share, contentDescription = "Share")
						}

						IconButton(
							onClick = {
								val send = Intent(Intent.ACTION_SEND).apply {
									type = "text/plain"
									putExtra(Intent.EXTRA_TEXT, transcriptText)
								}
								try {
									// Try direct to ChatGPT if installed
									send.`package` = "com.openai.chatgpt"
									context.startActivity(send)
								} catch (_: Exception) {
									context.startActivity(Intent.createChooser(send, "Send to ChatGPT"))
								}
							}
						) {
							Icon(Icons.Default.Send, contentDescription = "Send to ChatGPT")
						}
					}
				}
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                LoadingState(
                    modifier = Modifier.padding(innerPadding)
                )
            }
            error != null -> {
                ErrorState(
                    error = error!!,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            session != null -> {
                SessionDetailContent(
                    session = session!!,
                    transcriptText = transcriptText,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                TextButton(onClick = { deleteSession(); showDeleteDialog = false }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SessionDetailContent(
    session: TranscriptionSession,
    transcriptText: String,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault()) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Session header info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Session Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Start time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Started:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = dateFormat.format(session.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // End time if available
                session.endTime?.let { endTime ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ended:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = dateFormat.format(endTime),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Duration
                if (session.durationMs > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duration:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatDuration(session.durationMs),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status:", style = MaterialTheme.typography.bodyMedium)
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = when (session.status) {
                                    SessionStatus.IN_PROGRESS -> "In Progress"
                                    SessionStatus.COMPLETED -> "Completed"
                                    SessionStatus.FAILED -> "Failed"
                                    SessionStatus.CANCELLED -> "Cancelled"
                                }
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (session.status) {
                                SessionStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                SessionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                                SessionStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                                SessionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    )
                }
                
                // Chunk information
                if (session.chunkCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chunks:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${session.transcribedChunkCount}/${session.chunkCount} transcribed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Transcript content
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Transcription",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (transcriptText.isNotEmpty()) {
                    Text(
                        text = transcriptText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "No transcription available yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading session...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    
    return when {
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${seconds}s"
    }
}
