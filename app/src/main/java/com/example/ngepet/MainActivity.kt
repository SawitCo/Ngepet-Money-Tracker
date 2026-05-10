package com.example.ngepet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ngepet.presentation.ui.NgepetApp
import com.example.ngepet.ui.theme.NgepetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NgepetTheme {
                NgepetApp()
            }
        }
    }
}
