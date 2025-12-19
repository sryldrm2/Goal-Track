package com.esra.goaltrackproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.esra.goaltrackproject.data.GoalDatabase
import com.esra.goaltrackproject.presentation.navigation.GoalNavGraph
import com.esra.goaltrackproject.ui.theme.GoalTrackProjectTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Bildirim izni verildi")
        } else {
            android.util.Log.w("MainActivity", "Bildirim izni reddedildi - bildirimler çalışmayabilir")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            GoalTrackProjectTheme {
                val database = remember { GoalDatabase.getDatabase(this@MainActivity) }
                val goalDao = remember { database.goalDao() }
                
                GoalNavGraph(goalDao = goalDao)
            }
        }
    }
}
