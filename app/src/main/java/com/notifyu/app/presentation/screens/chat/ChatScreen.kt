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

    // Tracks which tab is currently selected (0 = Messages, 1 = People, 2 = Setting)
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Collecting necessary state values from ViewModel using StateFlow
    val organizationOwned by mainViewModel.organizationsOwned.collectAsState()
    val organizationId by mainViewModel.onOrganizationsClick.collectAsState()
    val organizationsMemberOf by mainViewModel.organizationsMemberOf.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()
    val messages by mainViewModel.onOrgMessages.collectAsState()
    val orgEmails by mainViewModel.orgEmails.collectAsState()
    val orgFcmTokens by mainViewModel.orgFcmTokens.collectAsState()
    val orgUids by mainViewModel.orgUids.collectAsState()
    val navEvent by mainViewModel.navigation.collectAsState()

    // Titles for the three tabs
    val tabTitles = listOf("Messages", "People", "Setting")

    // Find the selected organization using its ID from either owned or member-of list
    val selectedOrg = remember(organizationOwned, organizationsMemberOf, organizationId) {
        organizationOwned.find { it.id == organizationId }
            ?: organizationsMemberOf.find { it.id == organizationId }
    }

    // Check if the current user is the owner of the selected organization
    val isOwner = remember(selectedOrg, currentUser) {
        selectedOrg?.owner == currentUser?.uid
    }

    // Get avatar resources and fallback to default if not available
    val avatarList = getAvatarList()
    val avatarIndex = selectedOrg?.avatarIndex ?: 0
    val avatarRes = avatarList.getOrElse(avatarIndex) { avatarList[0] }

    // Coroutine scope for launching tasks
    val scope = rememberCoroutineScope()

    // When the last message changes, mark it as seen by the current user
    LaunchedEffect(selectedOrg?.lastMessage) {
        mainViewModel.updateSeenByForLastMessage(
            currentOrgId = selectedOrg?.id ?: "null",
            currentUser?.uid ?: "null"
        )
    }

    // When the list of members changes, fetch their data and determine roles
    LaunchedEffect(selectedOrg?.members?.toList()) {
        val memberIds = selectedOrg?.members ?: emptyList()
        mainViewModel.fetchAndCheckOrgUsers(memberIds, currentUser?.uid, isOwner)
    }

    // Handle navigation events triggered by the ViewModel
    LaunchedEffect(navEvent) {
        when (navEvent) {
            is AuthNavEvent.ToHome -> {
                navController.popBackStack()
                mainViewModel.resetNavigation()
            }

            // Other nav events (currently ignored)
            else -> {}
        }
    }

    // Layout column holding the tab and corresponding content
    Column {
        // Tab UI showing the current selected tab
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PrimaryColor
                )
            },
            divider = { HorizontalDivider(color = Color.Gray.copy(0.2f)) } // Divider line between tab and content
        ) {
            tabTitles.forEachIndexed { index, title ->
                // Only the owner can access all tabs; non-owners only see the Messages tab
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

        // Show tab content based on selected tab index
        when (selectedTabIndex) {
            0 -> EventMessagesTab(
                currentUserUid = currentUser?.uid ?: "null",
                isOwner = isOwner,
                organizationName = selectedOrg?.name ?: "null",
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
    currentUserUid: String,                  // The UID of the current logged-in user
    isOwner: Boolean,                        // Indicates if the current user is the owner (has permission to send messages)
    organizationName: String,               // Name of the organization (used in push notification title and message bubble)
    organizationId: String,                 // Unique identifier for the organization (used for push notifications)
    messages: List<Message>,                // List of chat messages to be displayed
    orgFcmTokens: List<String>,            // List of FCM tokens to send notifications to
    mainViewModel: MainViewModel           // ViewModel for handling business logic like sending messages and notifications
) {
    val textFieldValue = remember { mutableStateOf("") }  // Holds the current input text in the message field
    val context = LocalContext.current                    // Get the current context for showing toasts or passing to ViewModel
    val listState = rememberLazyListState()               // Remember the scroll state for the LazyColumn
    val scope = rememberCoroutineScope()                  // Coroutine scope to launch scrolling effect

    // Auto-scroll to the bottom of the chat list whenever the number of messages changes
    LaunchedEffect(messages.size) {
        scope.launch {
            if (messages.isNotEmpty()){
                listState.scrollToItem(messages.size - 1)  // Scroll to the last message
            }
        }
    }

    // Group messages by date and prepare a list that includes both date headers and chat messages
    val displayItems = remember(messages) {
        messages
            .groupBy { formatDateForGrouping(it.timestamp) }              // Group by formatted date
            .flatMap { (date, msgs) ->
                listOf(DisplayItem.DateHeader(date)) +                    // Add a date header
                        msgs.map { DisplayItem.ChatMessage(it) }                 // Add the messages under that date
            }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)    // Horizontal padding
            .fillMaxSize()                  // Fills the whole screen
            .imePadding(),                  // Avoids overlap with the keyboard
    ) {

        // Message list view
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),               // Fills remaining vertical space
            state = listState,             // Uses remembered scroll state
        ) {
            itemsIndexed(displayItems) { index, item ->
                when (item) {
                    is DisplayItem.DateHeader -> {
                        // Render the date header
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
                        // Display individual message bubble
                        MessageBubble(
                            organizationName = organizationName,
                            message = msg.content,
                            time = formatToTimeOnly(msg.timestamp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))  // Space between messages
                    }
                }
            }
        }

        // Message input and send section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),   // Padding below input box
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isOwner) {
                // If user is the owner, show input field and send button
                CustomBasicTextField(textFieldValue)

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)                       // Make the send button circular
                        .background(PrimaryColor)               // Primary color background
                        .clickable {
                            val text = textFieldValue.value.trim()     // Get trimmed input text
                            val tempValue = textFieldValue.value       // Temporarily hold the message
                            if (text.isNotEmpty()) {
                                textFieldValue.value = ""              // Clear input box

                                // Add message to Firestore or backend
                                mainViewModel.authAddMessage(
                                    content = text,
                                    senderId = currentUserUid
                                ) { isSuccess, message ->
                                    if (isSuccess) {
                                        // If message sent successfully, send push notification
                                        mainViewModel.authSendFcmPushNotification(
                                            context = context,
                                            targetTokens = orgFcmTokens,
                                            title = organizationName,
                                            body = tempValue,
                                            orgId = organizationId,
                                            orgName = organizationName
                                        )
                                    } else {
                                        // Show error toast if message failed to send
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center       // Icon centered inside the button
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,  // No accessibility description
                        tint = Color.White,         // White send icon
                    )
                }
            } else {
                // If user is not the owner, show a warning message
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
    membersEmail: List<String>, // List of member emails to be displayed
    membersUid: List<String>,   // Corresponding list of member UIDs
    mainViewModel: MainViewModel, // ViewModel instance to handle logic like removing a member
) {
    val context = LocalContext.current // Get the current context, although it's not used here
    var showLogoutDialog = remember { mutableStateOf(false) } // Controls visibility of the confirmation dialog
    val uidToRemove = remember { mutableStateOf("") } // Holds UID of the member to remove
    val removingMemberEmail = remember { mutableStateOf("") } // Holds email of the member to remove

    // Scrollable vertical list of members
    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Occupy the full height available
            .padding(bottom = 16.dp) // Add padding at the bottom
    ) {
        items(membersEmail.size) { index -> // For each member email
            val email = membersEmail[index] // Get email by index
            val uid = membersUid[index]     // Get corresponding UID

            // Display each member in a horizontal row
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp) // Add padding around the row
                    .fillMaxWidth() // Make the row full width
                    .background(Color.White), // Background color for the row
                verticalAlignment = Alignment.CenterVertically // Vertically align content to center
            ) {
                // Circular box with the first letter of the email
                Box(
                    modifier = Modifier
                        .size(35.dp) // Box size
                        .background(SurfaceColor.copy(0.5f), shape = CircleShape), // Light background with circle shape
                    contentAlignment = Alignment.Center // Center the content inside
                ) {
                    Text(
                        text = email.firstOrNull()?.toString() ?: "", // Display first character of the email or empty
                        color = Color.Black // Text color
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // Space between avatar and text

                // Column to display email and a static "Member" label
                Column {
                    Text(text = email, fontSize = 16.sp) // Display the email
                    Text(text = "Member", fontSize = 14.sp, color = Color.Gray) // Static role label
                }

                Spacer(modifier = Modifier.weight(1f)) // Push delete icon to the end

                // Delete icon button to trigger member removal
                IconButton(onClick = {
                    showLogoutDialog.value = true // Show the confirmation dialog
                    uidToRemove.value = uid // Set UID to be removed
                    removingMemberEmail.value = email // Set email for dialog display
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete, // Delete icon
                        contentDescription = "Remove", // Accessibility content
                        modifier = Modifier.size(18.dp) // Icon size
                    )
                }
            }

            // Divider between members
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp), // Padding for divider
                color = SurfaceColor.copy(0.2f) // Light colored divider
            )
        }
    }

    // Custom confirmation dialog when removing a member
    ConfirmationDialog(
        title = "Removing Member", // Dialog title
        text = "Are you sure you want to remove ${removingMemberEmail.value} from organization?", // Message body
        showDialog = showLogoutDialog.value, // Whether to show the dialog
        onDismiss = { showLogoutDialog.value = false }, // Close the dialog when dismissed
        onConfirm = {
            showLogoutDialog.value = false // Hide the dialog
            // Call ViewModel function to remove member by UID
            mainViewModel.authRemoveMemberFromOrganization(uidToRemove.value) { success, message ->
                // Result is received here, but not handled
            }
        }
    )
}



@Composable
fun EventSettingsTab(
    organization: Organization?,           // Nullable organization object containing org info
    currentUser: String,                   // Current user name or ID (used for display)
    organizationAvatar: Int,               // Resource ID of the organization's avatar
    mainViewModel: MainViewModel,          // ViewModel used to update organization avatar
    avatarList: List<Int>,                 // List of avatar drawable resource IDs
) {
    // Read-only state for organization name (initial value taken from nullable organization)
    val orgName by remember { mutableStateOf(organization?.name ?: "") }

    // Read-only state for organization code (fallback to empty string if null)
    val orgCode by remember { mutableStateOf(organization?.code ?: "") }

    // Controls the visibility of the avatar bottom sheet
    val showBottomSheet = remember { mutableStateOf(false) }

    // A gray color value used for disabled UI elements
    val grayColor = Color.Gray

    // Context used for showing toast messages
    val context = LocalContext.current

    // Main column layout of the settings screen
    Column(
        modifier = Modifier
            .fillMaxWidth()      // Occupies full width
            .padding(16.dp)      // Adds uniform padding
    ) {

        // Top section: Displays avatar image and an "Edit Icon" button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center    // Centers the column content horizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(organizationAvatar),  // Loads the image from resource ID
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)                         // Image size
                        .clip(CircleShape)                  // Makes it circular
                        .background(SurfaceColor.copy(0.5f)) // Applies a background tint
                )
                TextButton(onClick = { showBottomSheet.value = !showBottomSheet.value }) {
                    Text(text = "Edit Icon", color = PrimaryColor)  // Opens avatar selector
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))  // Space below avatar section

        // Section for organization name
        Text(text = "Organization name")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = orgName,
            onValueChange = {},          // No change allowed (read-only)
            enabled = false,             // Disables the field for editing
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

        // Section for organization code
        Text(text = "Organization Code")
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = orgCode,
            onValueChange = {},          // No change allowed
            enabled = false,             // Disabled input
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

        // Section showing the current user as organization admin
        Text(text = "Organization Admin")
        HorizontalDivider(color = Color.Gray)  // Divider below title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Displays the first character of the current user as avatar
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .background(SurfaceColor.copy(0.5f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser.firstOrNull()?.toString() ?: "",  // Safe extraction of first character
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))  // Space between avatar and text

            Column {
                Text(text = currentUser, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "Admin", fontSize = 14.sp, color = Color.Gray)  // Label as admin
            }
        }
    }

    // Bottom sheet for picking avatar icons
    AvatarPickerBottomSheet(
        showSheet = showBottomSheet.value,                 // Controls visibility
        onDismissRequest = { showBottomSheet.value = false },  // Callback to hide sheet
        avatarList = avatarList,                           // List of available avatars
        selectedAvatarIndex = organization?.avatarIndex ?: 0,  // Current selected index
        onAvatarSelected = { selectedIndex ->              // When new avatar is selected
            mainViewModel.authUpdateOrganizationAvatarIndex(
                orgId = organization?.id ?: "",            // Org ID or fallback to empty string
                newAvatarIndex = selectedIndex,            // New avatar index
                onResult = { success, message ->
                    // Show toast message with result (success or error)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    )
}


// Composable function to display a message bubble with organization name, message, and time
@Composable
fun MessageBubble(organizationName: String, message: String, time: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f) // Bubble width is 80% of screen width
            .background(
                Color.Gray.copy(alpha = 0.15f), // Light gray background with transparency
                shape = RoundedCornerShape(8.dp) // Rounded corners
            )
    ) {
        Text(
            text = message, // The message content
            color = Color.Black, // Text color
            fontSize = 16.sp, // Font size
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp) // Padding around text
        )

        Row(
            modifier = Modifier
                .fillMaxWidth() // Row takes full width of bubble
                .background(
                    PrimaryColor, // Uses predefined color for footer
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp) // Rounded only bottom corners
                )
                .padding(horizontal = 12.dp, vertical = 8.dp), // Padding inside footer
            horizontalArrangement = Arrangement.SpaceBetween // Distribute space between elements
        ) {
            Text(
//                text = organizationName, // This line is commented out and not used
                text = "Admin", // Static text used instead of organization name
                fontSize = 16.sp,
                color = Color.White, // White text color
                fontWeight = FontWeight.Medium, // Medium boldness
                maxLines = 1, // Ensure single-line name
                overflow = TextOverflow.Ellipsis, // Ellipsis if text is too long
                modifier = Modifier.weight(1f) // Take remaining space
            )
            Spacer(Modifier.width(16.dp)) // Space between name and time
            Text(
                text = time, // Time of message
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// Composable function to display a bottom sheet allowing user to pick an avatar
@OptIn(ExperimentalMaterial3Api::class) // Opt-in to experimental API
@Composable
fun AvatarPickerBottomSheet(
    showSheet: Boolean, // Controls whether the sheet is visible
    onDismissRequest: () -> Unit, // Callback when sheet is dismissed
    avatarList: List<Int>, // List of avatar drawable resource IDs
    selectedAvatarIndex: Int?, // Currently selected avatar index, nullable
    onAvatarSelected: (Int) -> Unit, // Callback when avatar is selected
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Only allow fully expanded or hidden state
    )
    val scope = rememberCoroutineScope() // Coroutine scope for launching suspend functions

    if (showSheet) {
        // Show the modal bottom sheet
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide() // Hide the sheet first
                    onDismissRequest() // Then call the provided dismiss callback
                }
            },
            sheetState = sheetState,
            containerColor = Color.White, // Background color of the sheet
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Rounded top corners
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding inside the sheet
            ) {
                Text(
                    text = "Select Avatar", // Title
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp) // Bottom space below title
                )

                FlowRow( // A row layout that wraps items to the next line
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Even spacing between items
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Space between rows
                    maxItemsInEachRow = 3 // Max 3 avatars per row
                ) {
                    avatarList.forEachIndexed { index, avatarRes ->
                        val isSelected = selectedAvatarIndex == index // Check if this avatar is selected
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Box size
                                .clip(CircleShape) // Make the avatar circular
                                .background(SurfaceColor.copy(0.5f)) // Semi-transparent background
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp, // Border if selected
                                    color = if (isSelected) PrimaryColor else Color.Transparent, // Color based on selection
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        sheetState.hide() // Hide sheet on click
                                        onAvatarSelected(index) // Notify selection
                                        onDismissRequest() // Close the sheet
                                    }
                                },
                            contentAlignment = Alignment.Center // Center avatar inside box
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes), // Load avatar image
                                contentDescription = "Avatar $index", // Accessibility label
                                modifier = Modifier
                                    .size(70.dp) // Slightly smaller than box
                                    .clip(CircleShape) // Make image circular
                            )
                        }
                    }
                }
            }
        }
    }
}
