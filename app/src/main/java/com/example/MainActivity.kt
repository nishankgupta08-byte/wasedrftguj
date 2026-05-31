package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FocusViewModel
import com.example.ui.FocusViewModelFactory
import com.example.ui.components.FocusDashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val viewModel: FocusViewModel by viewModels {
      FocusViewModelFactory(applicationContext)
    }

    setContent {
      val isBlackTheme by viewModel.isBlackTheme.collectAsStateWithLifecycle()
      MyApplicationTheme(darkTheme = isBlackTheme) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          FocusDashboard(viewModel = viewModel)
        }
      }
    }
  }
}
