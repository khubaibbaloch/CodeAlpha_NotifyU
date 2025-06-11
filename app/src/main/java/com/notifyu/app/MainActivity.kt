package com.notifyu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.main.MainScreen
import com.notifyu.app.presentation.theme.NotifyUTheme
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()
        mainViewModel.determineStartDestination()


        enableEdgeToEdge()
        setContent {
            NotifyUTheme {

                val startDestination by mainViewModel.startDestination.collectAsState()
                val navController = rememberNavController()

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
                MainScreen(navController,mainViewModel,startDestination!!)
            }
        }
    }
}
