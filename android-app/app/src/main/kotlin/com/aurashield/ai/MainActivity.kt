package com.aurashield.ai

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aurashield.ai.service.BackgroundMonitorService

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied. Background service alerts might not be visible.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()

        setContent {
            AuraShieldTheme {
                MainScreen()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startMonitorService() {
        val serviceIntent = Intent(this, BackgroundMonitorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopMonitorService() {
        val serviceIntent = Intent(this, BackgroundMonitorService::class.java).apply {
            action = BackgroundMonitorService.ACTION_STOP_SERVICE
        }
        startService(serviceIntent)
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BackgroundMonitorService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @Composable
    fun MainScreen() {
        var serviceRunning by remember { mutableStateOf(isServiceRunning()) }
        
        // Periodically poll service state
        LaunchedEffect(Unit) {
            while (true) {
                serviceRunning = isServiceRunning()
                kotlinx.coroutines.delay(1000)
            }
        }

        // Extract library versions safely
        val tfLiteVersion = remember {
            try {
                org.tensorflow.lite.TensorFlowLite.version()
            } catch (e: Throwable) {
                "Not Loaded: ${e.message}"
            }
        }

        val onnxVersion = remember {
            try {
                // Just verify if environment package loads
                ai.onnxruntime.OrtEnvironment.getEnvironment()
                "Loaded (1.17.1)"
            } catch (e: Throwable) {
                "Not Loaded: ${e.message}"
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "AuraShield AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Security Monitor Console",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Service Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (serviceRunning) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "MONITOR STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (serviceRunning) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (serviceRunning) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (serviceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (serviceRunning) "Active & Monitoring" else "Inactive / Idle",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { startMonitorService() },
                        enabled = !serviceRunning,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Monitor")
                    }

                    Button(
                        onClick = { stopMonitorService() },
                        enabled = serviceRunning,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Stop Monitor")
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Library / Dependency Status Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI/ML Runtime Roster",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        RuntimeInfoRow(name = "TensorFlow Lite", version = tfLiteVersion)
                        Spacer(modifier = Modifier.height(8.dp))
                        RuntimeInfoRow(name = "ONNX Runtime Mobile", version = onnxVersion)
                        Spacer(modifier = Modifier.height(8.dp))
                        RuntimeInfoRow(name = "Kotlin Coroutines", version = "1.7.3")
                    }
                }
            }
        }
    }

    @Composable
    fun RuntimeInfoRow(name: String, version: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Text(
                text = version,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AuraShieldTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF00C896),       // Electric Mint Green
        secondary = Color(0xFF00C896),
        background = Color(0xFF0B0F26),      // Deep Midnight Navy
        surface = Color(0xFF1F2336),         // Container background
        onPrimary = Color(0xFF000000),
        onBackground = Color(0xFFE2E8F0),
        onSurface = Color(0xFFFFFFFF),
        error = Color(0xFFFF6B6B),           // Neon Coral Red
        primaryContainer = Color(0xFF0D323F) // Muted teal-green container
    )
    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}
