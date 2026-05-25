package com.example.ngepet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ngepet.presentation.ui.NgepetApp
import com.example.ngepet.presentation.ui.theme.NgepetTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NgepetTheme {
                NgepetApp()
            }
        }
    }
}
