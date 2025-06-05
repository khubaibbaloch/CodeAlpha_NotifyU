package com.notifyu.app.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.notifyu.app.presentation.viewmodel.MainViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.theme.PrimaryColor

@Composable
fun CreateJoinOrgScreen(navController: NavController, mainViewModel: MainViewModel) {

    val context = LocalContext.current
    val isAddOrg by mainViewModel.isAddOrg.collectAsState()
    val organizationName = remember { mutableStateOf("") }
    val organizationCode = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (isAddOrg) {
            CreateOrg(
                organizationName = organizationName,
                organizationCode = organizationCode,
                onResult = {
//                    mainViewModel.addOrganization(
//                        name = organizationName.value,
//                        code = organizationCode.value,
//                        onResult = { isSuccess, message ->
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                            if (isSuccess){
//                                navController.navigate(MainScreenRoutes.HomeScreen.route)
//                            }
//                        })
                    mainViewModel.authAddOrganization(
                        name = organizationName.value,
                        code = organizationCode.value,
                        onResult = { isSuccess, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (isSuccess){
                                navController.navigate(MainScreenRoutes.HomeScreen.route)
                            }
                        })
                })
        } else {
            JoinOrg(
                organizationName = organizationName,
                organizationCode = organizationCode,
                onClick = {
//                    mainViewModel.joinOrganizationByNameAndCode(
//                        name = organizationName.value,
//                        code = organizationCode.value,
//                        onResult = { isSuccess, message ->
//                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//                            if (isSuccess) {
//                                navController.navigate(MainScreenRoutes.HomeScreen.route)
//                            }
//                        })
                    mainViewModel.authJoinOrganizationByNameAndCode(
                        name = organizationName.value,
                        code = organizationCode.value,
                        onResult = { isSuccess, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (isSuccess) {
                                navController.navigate(MainScreenRoutes.HomeScreen.route)
                            }
                        })
                })
        }
    }
}

@Composable
fun CreateOrg(
    organizationName: MutableState<String>,
    organizationCode: MutableState<String>,
    onResult: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = organizationName.value,
            onValueChange = { organizationName.value = it },
            label = { Text("Organization name") },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Red,

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
            value = organizationCode.value,
            onValueChange = { organizationCode.value = it },
            label = { Text("Organization code") },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Red,

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
            onClick = { onResult() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text(text = "Create organization", color = Color.White)
        }
    }
}

@Composable
fun JoinOrg(
    organizationName: MutableState<String>,
    organizationCode: MutableState<String>,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = organizationName.value,
            onValueChange = { organizationName.value = it },
            label = { Text("Organization name") },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Red,

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
            value = organizationCode.value,
            onValueChange = { organizationCode.value = it },
            label = { Text("Organization code") },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Red,

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
            onClick = { onClick() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text(text = "Join organization", color = Color.White)
        }
    }
}
