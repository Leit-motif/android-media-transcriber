package com.audioscribe.app.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import android.content.ClipboardManager
import android.content.ClipData
import com.audioscribe.app.service.AudioCaptureService
import com.audioscribe.app.ui.theme.AudioscribeTheme
import com.audioscribe.app.utils.ApiKeyStore
import com.audioscribe.app.utils.PermissionManager
import com.audioscribe.app.data.repository.SessionRepository
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.audioscribe.app.domain.permissions.PreflightIssue
import com.audioscribe.app.domain.permissions.checkPreflight
import com.audioscribe.app.domain.permissions.intentFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Main Activity for Audioscribe
 * Handles permissions and controls audio capture service
 */
@RequiresApi(Build.VERSION_CODES.Q)
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    private var isRecording by mutableStateOf(false)
    private var hasPermissions by mutableStateOf(false)
    private var transcriptionResult by mutableStateOf("")
    private var isProcessing by mutableStateOf(false)
    private var resultsReceiver: BroadcastReceiver? = null
    private lateinit var sessionRepository: SessionRepository
    
    // MediaProjection permission launcher
    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                startAudioCaptureService(result.resultCode, data)
            } else {
                Log.e(TAG, "MediaProjection data is null")
                Toast.makeText(this, "Failed to get screen capture permission", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "MediaProjection permission denied")
            Toast.makeText(this, "Screen capture permission is required for audio recording", Toast.LENGTH_LONG).show()
        }
    }
    
    // Runtime permissions launcher
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermissions = allGranted
        if (!allGranted) {
            Toast.makeText(this, "Microphone permission required.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize repository
        sessionRepository = SessionRepository(this)
        
        // Check initial permission state
        hasPermissions = PermissionManager.hasAllPermissions(this)
        
        setContent {
            AudioscribeTheme {
                AudioscribeScreen(
                    hasPermissions = hasPermissions,
                    isRecording = isRecording,
                    isProcessing = isProcessing,
                    transcriptionResult = transcriptionResult,
                    sessionRepository = sessionRepository,
                    onRequestPermissions = { requestPermissions() },
                    onStartRecording = { startRecording() },
                    onStopRecording = { stopRecording() },
                    onClearResults = { clearResults() },
                    requestMicPermission = {
                        val missing = PermissionManager.getMissingPermissions(this)
                        if (missing.isNotEmpty()) permissionsLauncher.launch(missing)
                    },
                    resolveIssue = { issue ->
                        intentFor(issue, this)?.let { startActivity(it) }
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check if service is running and sync state
        syncRecordingState()
        registerResultsReceiver()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle when activity is brought to front from notification
        syncRecordingState()
    }
    
    override fun onPause() {
        super.onPause()
        unregisterResultsReceiver()
    }
    
    private fun syncRecordingState() {
        val serviceRunning = isServiceRunning(AudioCaptureService::class.java)
        if (serviceRunning != isRecording) {
            isRecording = serviceRunning
            Log.i(TAG, "Synced recording state: isRecording = $isRecording")
        }
    }
    
    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    
    private fun requestPermissions() {
        val missingPermissions = PermissionManager.getMissingPermissions(this)
        if (missingPermissions.isNotEmpty()) {
            permissionsLauncher.launch(missingPermissions)
        } else {
            hasPermissions = true
        }
    }
    
    private fun startRecording() {
        if (!hasPermissions) {
            Toast.makeText(this, "Microphone permission required.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if device supports AudioPlaybackCapture
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Request MediaProjection permission for system audio capture
            val mediaProjectionIntent = PermissionManager.getMediaProjectionIntent(this)
            mediaProjectionLauncher.launch(mediaProjectionIntent)
        } else {
            // Fallback to microphone recording for older devices
            startMicrophoneRecording()
        }
    }
    
    private fun stopRecording() {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP_CAPTURE
        }
        startService(intent)
        isRecording = false
        
        // Check if API key is configured
        if (!ApiKeyStore.isConfigured(this)) {
            Toast.makeText(
                this, 
                "OpenAI API key not configured. Please add it in Settings to enable transcription.", 
                Toast.LENGTH_LONG
            ).show()
        } else {
            isProcessing = true
            Toast.makeText(this, "Recording stopped. Transcription starting...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearResults() {
        transcriptionResult = ""
        isProcessing = false
    }

    private fun registerResultsReceiver() {
        if (resultsReceiver != null) return
        resultsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                if (intent.action == AudioCaptureService.ACTION_TRANSCRIPTION_COMPLETE) {
                    val text = intent.getStringExtra(AudioCaptureService.EXTRA_TRANSCRIPTION_TEXT)
                    val error = intent.getStringExtra(AudioCaptureService.EXTRA_TRANSCRIPTION_ERROR)
                    if (!text.isNullOrEmpty()) {
                        if (transcriptionResult.isNotEmpty()) {
                            val ts = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                            transcriptionResult = buildString {
                                append(transcriptionResult)
                                append("\n\n— ")
                                append(ts)
                                append(" —\n")
                                append(text)
                            }
                        } else {
                            transcriptionResult = text
                        }
                        isProcessing = false
                    } else if (!error.isNullOrEmpty()) {
                        isProcessing = false
                        Toast.makeText(this@MainActivity, "Transcription failed: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        val filter = IntentFilter(AudioCaptureService.ACTION_TRANSCRIPTION_COMPLETE)
        registerReceiver(resultsReceiver, filter)
    }

    private fun unregisterResultsReceiver() {
        resultsReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) { }
        }
        resultsReceiver = null
    }
    
    private fun startAudioCaptureService(resultCode: Int, data: Intent) {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_START_CAPTURE
            putExtra(AudioCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data)
        }
        
        startForegroundService(intent)
        isRecording = true
        
        Log.i(TAG, "Audio capture service started")
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
    }
    
    private fun startMicrophoneRecording() {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_START_CAPTURE
            // No MediaProjection data needed for microphone mode
            putExtra(AudioCaptureService.EXTRA_RESULT_CODE, 0)
        }
        
        startForegroundService(intent)
        isRecording = true
        
        Log.i(TAG, "Microphone recording service started")
        Toast.makeText(this, "Recording from microphone", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPermissionRationale() {
        // For now, just show a toast. In a real app, you'd show a proper dialog.
        Toast.makeText(
            this,
            "Audioscribe needs microphone and notification permissions to record audio from other apps",
            Toast.LENGTH_LONG
        ).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioscribeScreen(
    hasPermissions: Boolean,
    isRecording: Boolean,
    isProcessing: Boolean,
    transcriptionResult: String,
    sessionRepository: SessionRepository,
    onRequestPermissions: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearResults: () -> Unit,
    requestMicPermission: () -> Unit,
    resolveIssue: (PreflightIssue) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Latest session combined transcript
    val latestSession by sessionRepository.getAllSessions().collectAsStateWithLifecycle(initialValue = emptyList())
    val latestSessionId = latestSession.firstOrNull()?.id
    val chunks by if (latestSessionId != null) {
        sessionRepository.getChunksForSession(latestSessionId).collectAsStateWithLifecycle(initialValue = emptyList())
    } else {
        mutableStateOf(emptyList())
    }
    
    val combinedTranscriptFromDB by remember(chunks) {
        derivedStateOf {
            if (chunks.isNotEmpty()) chunks.joinToString(" ") { it.text } else ""
        }
    }
    val displayTranscript = if (combinedTranscriptFromDB.isNotEmpty()) combinedTranscriptFromDB else transcriptionResult

    var showSheet by remember { mutableStateOf(false) }
    var issues by remember { mutableStateOf<List<PreflightIssue>>(emptyList()) }
    var elapsed by remember { mutableStateOf(0L) }

    // Tick timer while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsed = 0L
            while (isRecording) {
                delay(1000)
                elapsed += 1
            }
        }
    }

    fun runPreflight() {
        val now = checkPreflight(context)
        if (now.isEmpty()) {
            showSheet = false
            onStartRecording()
        } else {
            issues = now
            showSheet = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Audioscribe") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SessionListActivity::class.java))
                    }) { Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface) }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Record") },
                icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                onClick = { runPreflight() }
            )
        },
        bottomBar = {
            if (isRecording) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d:%02d", (elapsed / 60), (elapsed % 60)),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Pause placeholder (not implemented)
                            IconButton(onClick = { /* TODO: implement pause */ }) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            FilledTonalButton(onClick = onStopRecording) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Stop")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (displayTranscript.isNotEmpty() || isProcessing) Arrangement.Top else Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Capture and transcribe audio from other apps",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Transcription Results Section
            if (displayTranscript.isNotEmpty() || isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Results",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {
                                val latestSessionId = latestSession.firstOrNull()?.id
                                if (latestSessionId != null) {
                                    val intent = Intent(context, SessionDetailActivity::class.java).apply {
                                        putExtra("session_id", latestSessionId)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    context.startActivity(Intent(context, SessionListActivity::class.java))
                                }
                            }) { Text("View session →") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isProcessing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Processing recording...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (displayTranscript.isNotEmpty()) {
                            val scrollState = rememberScrollState()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 320.dp)
                                        .verticalScroll(scrollState)
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = displayTranscript,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 8
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSheet) {
            PermissionSheet(
                issues = issues,
                onIssueAction = { issue ->
                    when (issue) {
                        PreflightIssue.MicPermission -> requestMicPermission()
                        else -> resolveIssue(issue)
                    }
                },
                onClose = {
                    // Re-check after user attempts resolution
                    val now = checkPreflight(context)
                    issues = now
                    if (now.isEmpty()) { showSheet = false; onStartRecording() } else showSheet = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionSheet(
    issues: List<PreflightIssue>,
    onIssueAction: (PreflightIssue) -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Before recording", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            issues.forEach { issue ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (issue) {
                            PreflightIssue.MicPermission    -> "Microphone access"
                            PreflightIssue.AccessibilityOff -> "Enable Accessibility service"
                            PreflightIssue.OverlayOff       -> "Allow display over other apps"
                            PreflightIssue.BatteryOptimized -> "Ignore battery optimization"
                        }
                    )
                    TextButton(onClick = { onIssueAction(issue) }) { Text("Enable") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AudioscribeScreenPreview() {
    AudioscribeTheme {
        AudioscribeScreen(
            hasPermissions = true,
            isRecording = false,
            isProcessing = false,
            transcriptionResult = "",
            sessionRepository = SessionRepository(LocalContext.current), // Pass a dummy for preview
            onRequestPermissions = {},
            onStartRecording = {},
            onStopRecording = {},
            onClearResults = {},
            requestMicPermission = {},
            resolveIssue = {}
        )
    }
}

@Preview(showBackground = true, name = "With Results")
@Composable
fun AudioscribeScreenWithResultsPreview() {
    AudioscribeTheme {
        AudioscribeScreen(
            hasPermissions = true,
            isRecording = false,
            isProcessing = false,
            transcriptionResult = "Chunk 1 text...\n\n— 12:34:56 —\nChunk 2 text...",
            sessionRepository = SessionRepository(LocalContext.current), // Pass a dummy for preview
            onRequestPermissions = {},
            onStartRecording = {},
            onStopRecording = {},
            onClearResults = {},
            requestMicPermission = {},
            resolveIssue = {}
        )
    }
}
