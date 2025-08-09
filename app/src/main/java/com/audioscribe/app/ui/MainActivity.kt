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
                    onRequestPermissions = { requestPermissions() },
                    onStartRecording = { startRecording() },
                    onStopRecording = { stopRecording() }
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
    onRequestPermissions: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    isRecording -> "🔴 Recording audio from other apps..."
                    else -> "Ready to record"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            onRequestPermissions = {},
            onStartRecording = {},
            onStopRecording = {}
        )
    }
}
