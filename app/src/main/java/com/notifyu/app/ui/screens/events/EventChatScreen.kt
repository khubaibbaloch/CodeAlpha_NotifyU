package com.notifyu.app.ui.screens.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifyu.app.ui.screens.events.components.CustomBasicTextField
import com.notifyu.app.ui.theme.PrimaryColor
import com.notifyu.app.R

@Composable
fun EventChatScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Messages", "People", "Setting")

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PrimaryColor
                )
            },
            divider = {HorizontalDivider(color = Color.Gray.copy(0.2f))},

        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index) PrimaryColor else Color.Black
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> EventMessagesTab()
            1 -> EventPeopleTab()
            2 -> EventSettingsTab()
        }
    }
}

@Composable
fun EventMessagesTab() {
    var textFieldValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomBasicTextField()
            Column(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryColor, shape = CircleShape),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

    }
}

@Composable
fun EventPeopleTab() {
    LazyColumn {
        items(20) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .background(color = Color.Green, shape = CircleShape)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column {
                    Text(text = "Name", fontSize = 16.sp)
                    Text(text = "Announcement", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "Set", fontSize = 14.sp)

            }
        }
    }
}

@Composable
fun EventSettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top center image and edit button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(shape = CircleShape)
                )
                TextButton(onClick = { /* Handle edit icon click */ }) {
                    Text(text = "Edit Icon", color = PrimaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Organization Name
        Text(text = "Organization name",)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter organization name") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Code
        Text(text = "Organization Code")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter organization code") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Owner
        Text(text = "Organization Owner")
        HorizontalDivider( color= Color.Gray,)
        // Owner Info Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(color = Color.Green, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Name", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "Announcement", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
