package com.notifyu.app.presentation.screens.chat.components

import android.util.MutableBoolean
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifyu.app.presentation.navigation.navgraph.auth.AuthScreenRoutes
import com.notifyu.app.presentation.screens.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventChatScreenTopBar(
    title: String,
    onNavigationClick: () -> Unit,
    isOwner: Boolean,
    isDropDownMenuClicked: MutableState<Boolean>,
    onLeaveClick: () -> Unit,
    showLogoutDialog: MutableState<Boolean>,
    onConfirm: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        },
        actions = {
            if (!isOwner){
                IconButton(onClick = {isDropDownMenuClicked.value = true}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
                DropdownMenu(
                    expanded = isDropDownMenuClicked.value,
                    onDismissRequest = {isDropDownMenuClicked.value = false},
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Leave organization", color = Color.Black) },
                        onClick = onLeaveClick
                    )
                }
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )

    ConfirmationDialog(
        title = "Leaving Organization",
        text = "Are you sure you want to leave this organization?",
        showDialog = showLogoutDialog.value,
        onDismiss = { showLogoutDialog.value = false },
        onConfirm = {
            showLogoutDialog.value = false
            onConfirm()
        })

}
