package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WavveApp
import com.example.ui.PlayerViewModel
import com.example.ui.theme.MyApplicationTheme

@androidx.compose.material3.ExperimentalMaterial3Api
@androidx.compose.foundation.layout.ExperimentalLayoutApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val playerViewModel: PlayerViewModel = viewModel()
            
            // Initialize Player connection
            androidx.compose.runtime.LaunchedEffect(Unit) {
                playerViewModel.initialize(this@MainActivity.applicationContext)
            }
            
            MyApplicationTheme(settingsRepository = playerViewModel.settingsRepository) {
                WavveApp(playerViewModel)
            }
        }
    }
}
