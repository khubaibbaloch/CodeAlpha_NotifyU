package com.notifyu.app.presentation.screens.chat

import android.app.Notification
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.notifyu.app.presentation.screens.chat.components.CustomBasicTextField
import com.notifyu.app.presentation.theme.PrimaryColor
import com.notifyu.app.R
import com.notifyu.app.data.model.Message
import com.notifyu.app.data.model.Organization
import com.notifyu.app.presentation.navigation.navgraph.main.MainScreenRoutes
import com.notifyu.app.presentation.screens.chat.components.state.DisplayItem
import com.notifyu.app.presentation.screens.components.ConfirmationDialog
import com.notifyu.app.presentation.screens.main.components.getAvatarList
import com.notifyu.app.presentation.theme.SurfaceColor
import com.notifyu.app.presentation.viewmodel.MainViewModel
import com.notifyu.app.presentation.viewmodel.states.AuthNavEvent
import com.notifyu.app.utils.formatDateForGrouping
import com.notifyu.app.utils.formatToTimeOnly
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EventChatScreen(navController: NavController, mainViewModel: MainViewModel) {
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableStateOf(0) }
//    val orgEmails = remember { mutableStateListOf<String>() }
//    val orgFcmTokens = remember { mutableStateListOf<String>() }
//    val orgUids = remember { mutableStateListOf<String>() }

    val organizationOwned by mainViewModel.organizationsOwned.collectAsState()
    val organizationId by mainViewModel.onOrganizationsClick.collectAsState()
    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()
    val messages by mainViewModel.onOrgMessages.collectAsState()
    //val selectedOrg by mainViewModel.selectedOrganization.collectAsState()
    val orgEmails by mainViewModel.orgEmails.collectAsState()
    val orgFcmTokens by mainViewModel.orgFcmTokens.collectAsState()
    val orgUids by mainViewModel.orgUids.collectAsState()
//    val isOwner by mainViewModel.isOwner.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()

    val tabTitles = listOf("Messages", "People", "Setting")


//    val selectedOrganization = remember(organizationOwned, organizationsMemberOf, organizationId) {
//        organizationOwned.find { it.id == organizationId }
//            ?: organizationsMemberOf.find { it.id == organizationId }
//    }


    val selectedOrg = remember(organizationOwned, organizationsMemberOf, organizationId) {
        organizationOwned.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }
    }
    val isOwner = remember(selectedOrg, currentUser) {
        selectedOrg?.owner == currentUser?.uid
    }


    val avatarList = getAvatarList()
    val avatarIndex = selectedOrg?.avatarIndex ?: 0
    val avatarRes = avatarList.getOrElse(avatarIndex) { avatarList[0] }

    val scope = rememberCoroutineScope()


    LaunchedEffect(selectedOrg?.lastMessage) {
        mainViewModel.updateSeenByForLastMessage(
            currentOrgId = selectedOrg?.id ?: "null",
            currentUser?.uid ?: "null"
        )
    }

    LaunchedEffect(selectedOrg?.members?.toList()) {
        val memberIds = selectedOrg?.members ?: emptyList()
        mainViewModel.fetchAndCheckOrgUsers(memberIds, currentUser?.uid, isOwner)
    }

    LaunchedEffect(navEvent) {
        when (navEvent) {
            is AuthNavEvent.ToHome -> {
                navController.popBackStack()
                mainViewModel.resetNavigation()
            }

            else -> {}
        }
    }


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
            divider = { HorizontalDivider(color = Color.Gray.copy(0.2f)) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                val shouldEnable = isOwner || index == 0

                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { if (shouldEnable) selectedTabIndex = index },
                    enabled = shouldEnable,
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index) PrimaryColor
                            else Color.Black.copy(alpha = if (shouldEnable) 1f else 0.4f)
                        )
                    }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> EventMessagesTab(
                currentUserUid = currentUser?.uid ?: "null",
                isOwner = isOwner,
                organizationName = selectedOrg?.name ?: "null",
//                messages = messages,
                messages = selectedOrg?.messages ?: emptyList(),
                orgFcmTokens = orgFcmTokens,
                mainViewModel = mainViewModel,
                organizationId = selectedOrg?.id ?: "null"
            )

            1 -> EventPeopleTab(
                membersEmail = orgEmails,
                membersUid = orgUids,
                mainViewModel = mainViewModel
            )

            2 -> EventSettingsTab(
                organization = selectedOrg,
                currentUser = currentUser?.email.toString(),
                organizationAvatar = avatarRes,
                mainViewModel = mainViewModel,
                avatarList = avatarList
            )
        }
    }
}



@Composable
fun EventMessagesTab(
    currentUserUid: String,
    isOwner: Boolean,
    organizationName: String,
    organizationId: String,
    messages: List<Message>,
    orgFcmTokens: List<String>,
    mainViewModel: MainViewModel,
) {
    val textFieldValue = remember { mutableStateOf("") }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        scope.launch {
            if (messages.isNotEmpty()){
                listState.scrollToItem(messages.size - 1)
            }
        }
    }


    val displayItems = remember(messages) {
        messages
            .groupBy { formatDateForGrouping(it.timestamp) }
            .flatMap { (date, msgs) ->
                listOf(DisplayItem.DateHeader(date)) + msgs.map { DisplayItem.ChatMessage(it) }
            }
    }


    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .imePadding(),
    ) {


        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState,
        ) {
            itemsIndexed(displayItems) { index, item ->
                when (item) {
                    is DisplayItem.DateHeader -> {
                        Text(
                            text = item.date,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    is DisplayItem.ChatMessage -> {
                        val msg = item.message
                        MessageBubble(
                            organizationName = organizationName,
                            message = msg.content,
                            time = formatToTimeOnly(msg.timestamp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isOwner) {
                CustomBasicTextField(textFieldValue)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryColor)
                        .clickable {
                            val text = textFieldValue.value.trim()
                            val tempValue = textFieldValue.value
                            if (text.isNotEmpty()) {
                                textFieldValue.value = ""
                                mainViewModel.authAddMessage(
                                    content = text,
                                    senderId = currentUserUid
                                ) { isSuccess, message ->
                                    if (isSuccess) {
                                        mainViewModel.authSendFcmPushNotification(
                                            context = context,
                                            targetTokens = orgFcmTokens,
                                            title = organizationName,
                                            body = tempValue,
                                            orgId = organizationId,
                                            orgName = organizationName
                                        )
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            } else {
                Text(
                    text = "You are not allowed to send messages.",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

        }
    }
}

@Composable
fun EventPeopleTab(
    membersEmail: List<String>,
    membersUid: List<String>,
    mainViewModel: MainViewModel,
) {
    val context = LocalContext.current
    var showLogoutDialog = remember { mutableStateOf(false) }
    val uidToRemove = remember { mutableStateOf("") }
    val removingMemberEmail = remember { mutableStateOf("") }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        items(membersEmail.size) { index ->
            val email = membersEmail[index]
            val uid = membersUid[index]

            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .background(SurfaceColor.copy(0.5f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = email.firstOrNull()?.toString() ?: "",
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = email, fontSize = 16.sp)
                    Text(text = "Member", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    showLogoutDialog.value = true
                    uidToRemove.value = uid
                    removingMemberEmail.value = email
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = SurfaceColor.copy(0.2f)
            )
        }
    }

    ConfirmationDialog(
        title = "Removing Member",
        text = "Are you sure you want to remove ${removingMemberEmail.value} from organization?",
        showDialog = showLogoutDialog.value,
        onDismiss = { showLogoutDialog.value = false },
        onConfirm = {
            showLogoutDialog.value = false
            mainViewModel.authRemoveMemberFromOrganization(uidToRemove.value) { success, message -> }

        })

}


@Composable
fun EventSettingsTab(
    organization: Organization?,
    currentUser: String,
    organizationAvatar: Int,
    mainViewModel: MainViewModel,
    avatarList: List<Int>,
) {
    val orgName by remember { mutableStateOf(organization?.name ?: "") }
    val orgCode by remember { mutableStateOf(organization?.code ?: "") }
    val showBottomSheet = remember { mutableStateOf(false) }
    val grayColor = Color.Gray
    val context = LocalContext.current



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
                    painter = painterResource(organizationAvatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor.copy(0.5f))

                )
                TextButton(onClick = { showBottomSheet.value = !showBottomSheet.value }) {
                    Text(text = "Edit Icon", color = PrimaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Organization Name
        Text(text = "Organization name")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = orgName,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter organization name", color = grayColor) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray,
                disabledBorderColor = Color.Gray,
                disabledPlaceholderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTrailingIconColor = Color.Gray,
                disabledLeadingIconColor = Color.Gray,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Code
        Text(text = "Organization Code")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = orgCode,
            onValueChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter organization code", color = grayColor) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.Gray,
                disabledBorderColor = Color.Gray,
                disabledPlaceholderColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTrailingIconColor = Color.Gray,
                disabledLeadingIconColor = Color.Gray,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Organization Owner
        Text(text = "Organization Admin")
        HorizontalDivider(color = Color.Gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(SurfaceColor.copy(0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser.firstOrNull()?.toString() ?: "",
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = currentUser, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "Admin", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }

    AvatarPickerBottomSheet(
        showSheet = showBottomSheet.value,
        onDismissRequest = { showBottomSheet.value = false },
        avatarList = avatarList,
        selectedAvatarIndex = organization?.avatarIndex ?: 0,
        onAvatarSelected = { selectedIndex ->
            mainViewModel.authUpdateOrganizationAvatarIndex(
                orgId = organization?.id ?: "",
                newAvatarIndex = selectedIndex,
                onResult = { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    )
}


@Composable
fun MessageBubble(organizationName: String, message: String, time: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(
                Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Text(
            text = message,
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    PrimaryColor,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
//                text = organizationName,
                text = "Admin",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = time,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerBottomSheet(
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    avatarList: List<Int>,
    selectedAvatarIndex: Int?, // <- new parameter to indicate selected avatar
    onAvatarSelected: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Avatar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 3
                ) {
                    avatarList.forEachIndexed { index, avatarRes ->
                        val isSelected = selectedAvatarIndex == index
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SurfaceColor.copy(0.5f))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) PrimaryColor else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                        onAvatarSelected(index)
                                        onDismissRequest()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = "Avatar $index",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}
