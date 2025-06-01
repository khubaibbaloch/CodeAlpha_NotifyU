package com.notifyu.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.R
import com.notifyu.app.navigation.navgraph.AuthScreenRoute
import com.notifyu.app.ui.screens.auth.components.LottieAnimations
import com.notifyu.app.ui.theme.BackgroundColor
import com.notifyu.app.ui.theme.PrimaryColor
import com.notifyu.app.viewmodel.MainViewModel

@Composable
fun ResetPasswordScreen(navController: NavController, mainViewModel: MainViewModel) {

    val context = LocalContext.current

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

    // Password visibility toggles
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    val currentUser by remember { mutableStateOf(mainViewModel.auth.currentUser) }


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
            LottieAnimations(modifier = Modifier.weight(1f), R.raw.reset_password)
            OutlinedTextField(
                value = email.value,
                onValueChange = {
                    email.value = it
                },
                label = { Text("Email") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Red,  // Set error text color to red

                    cursorColor = Color.Black,
                    errorCursorColor = Color.Red,

                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red,

                    focusedLeadingIconColor = Color.Black,
                    unfocusedLeadingIconColor = Color.Gray,
                    errorLeadingIconColor = Color.Red,

                    focusedTrailingIconColor = Color.Black,
                    unfocusedTrailingIconColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray,
                    errorTrailingIconColor = Color.Red,

                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    errorLabelColor = Color.Red,

                    )
            )

            OutlinedTextField(
                value = password.value,
                onValueChange = {
                    password.value = it
                },
                label = { Text("Password") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible.value)
                        painterResource(R.drawable.ic_visibility)
                    else painterResource(R.drawable.ic_visibility_off)
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(
                            painter = image,
                            contentDescription = if (passwordVisible.value) "Hide password" else "Show password",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Red,  // Set error text color to red

                    cursorColor = Color.Black,
                    errorCursorColor = Color.Red,

                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red,

                    focusedLeadingIconColor = Color.Black,
                    unfocusedLeadingIconColor = Color.Gray,
                    errorLeadingIconColor = Color.Red,

                    focusedTrailingIconColor = Color.Black,
                    unfocusedTrailingIconColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray,
                    errorTrailingIconColor = Color.Red,

                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    errorLabelColor = Color.Red,

                    )
            )

            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = {
                    confirmPassword.value = it
                },
                label = { Text("Confirm Password") },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible.value)
                        painterResource(R.drawable.ic_visibility)
                    else painterResource(R.drawable.ic_visibility_off)
                    IconButton(onClick = {
                        confirmPasswordVisible.value = !confirmPasswordVisible.value
                    }) {
                        Icon(
                            painter = image,
                            contentDescription = if (confirmPasswordVisible.value) "Hide password" else "Show password",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Red,  // Set error text color to red

                    cursorColor = Color.Black,
                    errorCursorColor = Color.Red,

                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red,

                    focusedLeadingIconColor = Color.Black,
                    unfocusedLeadingIconColor = Color.Gray,
                    errorLeadingIconColor = Color.Red,

                    focusedTrailingIconColor = Color.Black,
                    unfocusedTrailingIconColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray,
                    errorTrailingIconColor = Color.Red,

                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    errorLabelColor = Color.Red,

                    )
            )

            Button(
                onClick = {

                    if (currentUser == null) {
                        Toast.makeText(context, "No user is signed in", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (email.value.isEmpty() || password.value.isEmpty() || confirmPassword.value.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (email.value != currentUser!!.email) {
                        Toast.makeText(
                            context,
                            "Email does not match the signed-in user",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (password.value != confirmPassword.value) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    mainViewModel.updatePassword(password.value) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            Toast.makeText(context, "Passwords updated", Toast.LENGTH_SHORT).show()
                            navController.navigate(AuthScreenRoute.LoginScreen.route)
                        }
                    }
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account? ")
                Text(
                    text = "Login",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(AuthScreenRoute.LoginScreen.route)
                    }
                )
            }

        }
    }
}