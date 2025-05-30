package com.notifyu.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.ui.screens.auth.components.LottieAnimations
import com.notifyu.app.ui.theme.BackgroundColor
import com.notifyu.app.ui.theme.PrimaryColor
import com.notifyu.app.R
import com.notifyu.app.navigation.navgraph.AuthScreenRoute

@Composable
fun LoginScreen(navController: NavController) {
    Scaffold(containerColor = BackgroundColor) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Notifyu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
            LottieAnimations(modifier = Modifier.weight(1f),R.raw.login_lottie)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Forget Password",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(AuthScreenRoute.VerifyEmailScreen.route)
                    }
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                onClick = {},
                shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text ="Don't have an account? ",)
                Text(
                    text = "Signup",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        // Navigate to login screen here
                        navController.navigate(AuthScreenRoute.SignupScreen.route)
                    }
                )
            }

        }
    }
}