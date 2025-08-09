package com.audioscribe.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.audioscribe.app.service.AudioCaptureService
import com.audioscribe.app.ui.theme.AudioscribeTheme
import com.audioscribe.app.utils.PermissionManager

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
        
        if (allGranted) {
            Log.i(TAG, "All permissions granted")
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            val deniedPermissions = permissions.filterValues { !it }.keys
            Log.w(TAG, "Permissions denied: $deniedPermissions")
            Toast.makeText(this, "Some permissions were denied. Please grant them to use the app.", Toast.LENGTH_LONG).show()
            
            if (PermissionManager.shouldShowRationale(this)) {
                // Show rationale dialog
                showPermissionRationale()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check initial permission state
        hasPermissions = PermissionManager.hasAllPermissions(this)
        
        setContent {
            AudioscribeTheme {
                AudioscribeScreen(
                    hasPermissions = hasPermissions,
                    isRecording = isRecording,
                    isProcessing = isProcessing,
                    transcriptionResult = transcriptionResult,
                    onRequestPermissions = { requestPermissions() },
                    onStartRecording = { startRecording() },
                    onStopRecording = { stopRecording() },
                    onClearResults = { clearResults() }
                )
            }
        }
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
            Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Request MediaProjection permission
        val mediaProjectionIntent = PermissionManager.getMediaProjectionIntent(this)
        mediaProjectionLauncher.launch(mediaProjectionIntent)
    }
    
    private fun stopRecording() {
        val intent = Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP_CAPTURE
        }
        startService(intent)
        isRecording = false
        isProcessing = true
        
        // Simulate transcription processing for now
        // In the real implementation, this would be handled by the transcription service
        simulateTranscriptionProcessing()
    }
    
    private fun simulateTranscriptionProcessing() {
        // Simulate processing delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            transcriptionResult = "Audio recording completed and saved.\n\nTranscription will be available once the transcription service is implemented in Task #4.\n\nRecorded file: audioscribe_${System.currentTimeMillis()}.wav"
            isProcessing = false
            Toast.makeText(this, "Recording saved successfully", Toast.LENGTH_SHORT).show()
        }, 2000)
    }
    
    private fun clearResults() {
        transcriptionResult = ""
        isProcessing = false
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
    
    private fun showPermissionRationale() {
        // For now, just show a toast. In a real app, you'd show a proper dialog.
        Toast.makeText(
            this,
            "Audioscribe needs microphone and notification permissions to record audio from other apps",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun AudioscribeScreen(
    hasPermissions: Boolean,
    isRecording: Boolean,
    isProcessing: Boolean,
    transcriptionResult: String,
    onRequestPermissions: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearResults: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (transcriptionResult.isNotEmpty()) Arrangement.Top else Arrangement.Center
        ) {
            Text(
                text = "Audioscribe",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Capture and transcribe audio from other apps",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Permission status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasPermissions) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (hasPermissions) "✓ Permissions Granted" else "⚠ Permissions Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (hasPermissions) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    if (!hasPermissions) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Microphone, notification, and background service permissions are required",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main action buttons
            if (!hasPermissions) {
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions")
                }
            } else {
                if (!isRecording) {
                    Button(
                        onClick = onStartRecording,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Recording")
                    }
                } else {
                    Button(
                        onClick = onStopRecording,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Stop Recording")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status text
            Text(
                text = when {
                    !hasPermissions -> "Please grant permissions to continue"
                    isProcessing -> "⏳ Processing recording..."
                    isRecording -> "🔴 Recording audio from other apps..."
                    else -> "Ready to record"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Transcription Results Section
            if (transcriptionResult.isNotEmpty() || isProcessing) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
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
                            
                            if (transcriptionResult.isNotEmpty()) {
                                TextButton(onClick = onClearResults) {
                                    Text("Clear")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isProcessing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                        } else if (transcriptionResult.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = transcriptionResult,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
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
            onRequestPermissions = {},
            onStartRecording = {},
            onStopRecording = {},
            onClearResults = {}
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
            transcriptionResult = "Sample transcription result would appear here once the transcription service is implemented.",
            onRequestPermissions = {},
            onStartRecording = {},
            onStopRecording = {},
            onClearResults = {}
        )
    }
}
