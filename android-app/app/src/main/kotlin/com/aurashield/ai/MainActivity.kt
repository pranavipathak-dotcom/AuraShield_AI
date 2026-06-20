package com.aurashield.ai

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aurashield.ai.service.BackgroundMonitorService
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val view = android.view.LayoutInflater.from(context).inflate(R.layout.activity_main, null)
                
                // Set up pulsing scale animation on the central shield button view
                val shieldButton = view.findViewById<ImageButton>(R.id.btnShield)
                val shieldPulseAnim = AnimationUtils.loadAnimation(context, R.anim.shield_pulse)
                shieldButton.startAnimation(shieldPulseAnim)

                // Set up pulsing alpha animation on the status indicators
                val statusLayout = view.findViewById<android.view.View>(R.id.layoutStatus)
                val subtitlePulseAnim = AnimationUtils.loadAnimation(context, R.anim.subtitle_pulse)
                statusLayout.startAnimation(subtitlePulseAnim)

                // Start background animation for audio wave bars (20 elements for smooth visualizer)
                val waveBars = listOf(
                    view.findViewById<android.view.View>(R.id.waveBar1),
                    view.findViewById<android.view.View>(R.id.waveBar2),
                    view.findViewById<android.view.View>(R.id.waveBar3),
                    view.findViewById<android.view.View>(R.id.waveBar4),
                    view.findViewById<android.view.View>(R.id.waveBar5),
                    view.findViewById<android.view.View>(R.id.waveBar6),
                    view.findViewById<android.view.View>(R.id.waveBar7),
                    view.findViewById<android.view.View>(R.id.waveBar8),
                    view.findViewById<android.view.View>(R.id.waveBar9),
                    view.findViewById<android.view.View>(R.id.waveBar10),
                    view.findViewById<android.view.View>(R.id.waveBar11),
                    view.findViewById<android.view.View>(R.id.waveBar12),
                    view.findViewById<android.view.View>(R.id.waveBar13),
                    view.findViewById<android.view.View>(R.id.waveBar14),
                    view.findViewById<android.view.View>(R.id.waveBar15),
                    view.findViewById<android.view.View>(R.id.waveBar16),
                    view.findViewById<android.view.View>(R.id.waveBar17),
                    view.findViewById<android.view.View>(R.id.waveBar18),
                    view.findViewById<android.view.View>(R.id.waveBar19),
                    view.findViewById<android.view.View>(R.id.waveBar20)
                )
                
                // Launch coroutine to animate wave bars heights
                (context as? androidx.lifecycle.LifecycleOwner)?.lifecycleScope?.launch {
                    val random = java.util.Random()
                    while (isActive) {
                        for (bar in waveBars) {
                            bar?.let {
                                val newScaleY = 0.2f + random.nextFloat() * 1.3f
                                it.animate()
                                    .scaleY(newScaleY)
                                    .setDuration(120)
                                    .start()
                            }
                        }
                        kotlinx.coroutines.delay(130)
                    }
                }

                // Handle click action to toggle service
                shieldButton.setOnClickListener {
                    if (isServiceRunning()) {
                        stopMonitorService()
                        Toast.makeText(context, "Stopping Security Monitor...", Toast.LENGTH_SHORT).show()
                    } else {
                        startMonitorService()
                        Toast.makeText(context, "Starting Security Monitor...", Toast.LENGTH_SHORT).show()
                    }
                    serviceRunning = isServiceRunning()
                }

                view
            },
            update = { view ->
                // Update views based on service state
                val statusText = view.findViewById<TextView>(R.id.tvStatusText)
                val statusDot = view.findViewById<android.view.View>(R.id.viewStatusDot)
                val shieldButton = view.findViewById<ImageButton>(R.id.btnShield)
                val shieldGlow = view.findViewById<android.view.View>(R.id.viewShieldGlow)

                if (serviceRunning) {
                    statusText.text = "Live Edge Protection Active"
                    statusText.setTextColor(ContextCompat.getColor(view.context, R.color.mint_green))
                    statusDot.setBackgroundColor(ContextCompat.getColor(view.context, R.color.mint_green))
                    shieldButton.setImageResource(R.drawable.ic_shield)
                    // Show active glow ring
                    shieldGlow.visibility = android.view.View.VISIBLE
                } else {
                    statusText.text = "Protection Inactive"
                    statusText.setTextColor(ContextCompat.getColor(view.context, R.color.neon_coral))
                    statusDot.setBackgroundColor(ContextCompat.getColor(view.context, R.color.neon_coral))
                    shieldButton.setImageResource(android.R.drawable.ic_lock_lock)
                    // Hide active glow ring
                    shieldGlow.visibility = android.view.View.INVISIBLE
                }
            }
        )
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
