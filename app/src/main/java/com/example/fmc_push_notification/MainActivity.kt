package com.example.fmc_push_notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.fmc_push_notification.ui.theme.Fmc_Push_NotificationTheme
import com.onesignal.OneSignal

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OneSignal.initWithContext(this, "5044e189-34f6-4e8f-a3b4-e515805f445f")
        setContent {
            Fmc_Push_NotificationTheme {
                val navController = rememberNavController()
                Navigation(navController)
            }
        }
    }
}

