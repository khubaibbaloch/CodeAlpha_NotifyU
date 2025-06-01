package com.notifyu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.notifyu.app.navigation.navgraph.RootNavHost
import com.notifyu.app.ui.screens.main.MainScreen
import com.notifyu.app.ui.theme.NotifyUTheme
import com.notifyu.app.ui.theme.SurfaceColor
import com.notifyu.app.viewmodel.MainViewModel
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
