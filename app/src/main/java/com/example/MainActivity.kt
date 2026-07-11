package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StatusViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    private val viewModel: StatusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val imageLoader = coil.ImageLoader.Builder(this)
            .components {
                add(coil.decode.VideoFrameDecoder.Factory())
            }
            .build()
        coil.Coil.setImageLoader(imageLoader)
        
        enableEdgeToEdge()
        
        setContent {
            // Observe the user's theme selection flow
            val themePreference by viewModel.themePreference.collectAsState()
            
            // Determine the final dark/light status based on selection and system theme
            val useDarkTheme = when (themePreference) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = useDarkTheme) {
                MainScreen(
                    viewModel = viewModel,
                    isDarkTheme = useDarkTheme,
                    onToggleTheme = { isDark ->
                        viewModel.setTheme(if (isDark) "dark" else "light")
                    }
                )
            }
        }
    }
}
