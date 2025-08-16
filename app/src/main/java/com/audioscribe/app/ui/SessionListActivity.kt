package com.audioscribe.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.audioscribe.app.data.database.entity.TranscriptionSession
import com.audioscribe.app.data.database.entity.SessionStatus
import com.audioscribe.app.data.repository.SessionRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class SessionListActivity : ComponentActivity() {
    
    private lateinit var sessionRepository: SessionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionRepository = SessionRepository(this)
        
        setContent {
            SessionListScreen(
                onBack = { finish() },
                onSessionClick = { sessionId ->
                    val intent = Intent(this@SessionListActivity, SessionDetailActivity::class.java).apply {
                        putExtra("session_id", sessionId)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionListScreen(
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val sessionRepository = remember { SessionRepository(context) }
    
    val sessions by sessionRepository.getAllSessions().collectAsStateWithLifecycle(initialValue = emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transcription History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (sessions.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions) { session ->
                    SessionItem(
                        session = session,
                        onClick = { onSessionClick(session.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionItem(
    session: TranscriptionSession,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title or timestamp
            Text(
                text = session.title ?: "Session ${session.id}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date and time
            Text(
                text = dateFormat.format(session.startTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status chip
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
                
                // Progress info
                if (session.chunkCount > 0) {
                    Text(
                        text = "${session.transcribedChunkCount}/${session.chunkCount} chunks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Duration if available
            if (session.durationMs > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Duration: ${formatDuration(session.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No transcription sessions yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start recording to create your first session",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
