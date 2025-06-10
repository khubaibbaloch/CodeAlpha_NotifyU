package com.notifyu.app.presentation.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifyu.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenTopBar(
    title: String,
    onNavigationClick: () -> Unit,
    hasElevation: Boolean,
    isDropDownMenuClicked: Boolean,
    onMoreVertClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { onNavigationClick() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = null,
                    modifier = Modifier

                        .size(20.dp),
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
            IconButton(onClick = onMoreVertClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
            }
            DropdownMenu(
                expanded = isDropDownMenuClicked,
                onDismissRequest = onDismissRequest,
                modifier = Modifier.background(Color.White)
            ) {
                DropdownMenuItem(
                    text = { Text("Setting", color = Color.Black) },
                    onClick = onLeaveClick
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        modifier = Modifier
            .graphicsLayer {
                if (hasElevation) {
                    shadowElevation = 2.dp.toPx()
                    shape = RectangleShape
                    clip = false
                }

            }

    )
}