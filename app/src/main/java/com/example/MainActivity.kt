package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MiniWeatherTheme
import com.example.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF60A5FA)
                ) {
                    HomeScreen(viewModel = weatherViewModel)
                }
            }
        }
    }
}
