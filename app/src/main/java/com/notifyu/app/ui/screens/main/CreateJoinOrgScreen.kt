package com.notifyu.app.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.notifyu.app.viewmodel.MainViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun CreateJoinOrgScreen(navController: NavController, mainViewModel: MainViewModel) {

    val isAddOrg by remember { mutableStateOf(true) }
    val organizationName = remember { mutableStateOf("") }
    val organizationCode = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        if (isAddOrg) {
            CreateOrg(organizationName = organizationName, organizationCode = organizationCode)
        } else {
            JoinOrg()
        }
    }
}

@Composable
fun CreateOrg(organizationName: MutableState<String>, organizationCode: MutableState<String>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Add Organization")
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

        Button(onClick = {}) {
            Text(text = "Create organization")
        }
    }
}

@Composable
fun JoinOrg() {

}