package com.notifyu.app.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.notifyu.app.navigation.navgraph.MainScreenRoute
import com.notifyu.app.navigation.navgraph.RootNavHost
import com.notifyu.app.ui.screens.components.EventChatScreenTopBar
import com.notifyu.app.ui.screens.components.MainScreenTopBar
import com.notifyu.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, mainViewModel: MainViewModel) {
    Column() {
        Button(onClick = {
            mainViewModel.auth.signOut()
        }) { }
        Text(text = "join or create")
    }
}
