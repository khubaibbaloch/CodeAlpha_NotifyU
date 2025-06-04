package com.notifyu.app.ui.screens.main

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notifyu.app.data.model.SelectedScreen
import com.notifyu.app.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, mainViewModel: MainViewModel) {
    val selectedScreenState = remember { mutableStateOf(SelectedScreen.None.value) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        mainViewModel.fetchSelectedScreenForCurrentUser { selectedScreen ->
            selectedScreenState.value = selectedScreen ?: SelectedScreen.None.value
        }
    }

    LaunchedEffect(selectedScreenState.value) {
        when (selectedScreenState.value) {
            SelectedScreen.Owned.value -> navController.navigate(MainScreenRoutes.OrganizationOwnedScreen.route)
            SelectedScreen.Joined.value -> navController.navigate(MainScreenRoutes.OrganizationJoinedScreen.route)
        }
    }


    if (selectedScreenState.value == SelectedScreen.None.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Welcome! Create or Join an Organization")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    mainViewModel.auth.signOut()
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sign Out")
                }
            }
        }
    }
}
