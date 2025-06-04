package com.notifyu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.notifyu.app.presentation.screens.main.MainScreen
import com.notifyu.app.presentation.theme.NotifyUTheme
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        enableEdgeToEdge()
        setContent {
            NotifyUTheme {
                // Set status bar color
                val systemUiController = rememberSystemUiController()
                val statusBarColor = SurfaceColor
                val useDarkIcons = false

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor,
                        darkIcons = useDarkIcons
                    )
                }

                val navController = rememberNavController()
                MainScreen(navController,mainViewModel)
            }
        }
    }
}
