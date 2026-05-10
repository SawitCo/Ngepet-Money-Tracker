package com.example.ngepet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.ngepet.presentation.MainViewModel
import com.example.ngepet.presentation.ui.NgepetApp
import com.example.ngepet.presentation.ui.theme.NgepetTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NgepetTheme {
                NgepetApp(viewModel = viewModel)
            }
        }
    }
}
        }
    }
}
