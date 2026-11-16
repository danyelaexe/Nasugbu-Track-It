package com.example.retailbusinessandservicecoveragemapper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon as OsmPolygon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.retailbusinessandservicecoveragemapper.ui.theme.RetailBusinessAndServiceCoverageMapperTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

val CATEGORIES = listOf("All", "Grocery", "Hardware", "Pharmacy", "Service Center", "General Retail", "Stationery", "Printing Shop")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetailBusinessAndServiceCoverageMapperTheme {
                RetailBusinessAndServiceCoverageMapperApp()
            }
        }
    }
}

private enum class AppTab(val label: String, val badgeText: String) {
    DASHBOARD("Dashboard", "📊"),
    BRANCHES("Branches", "🏪"),
    MAP("Coverage Map", "🗺️"),
}

@Composable
fun RetailBusinessAndServiceCoverageMapperApp() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }

    var isAdminLoggedIn by rememberSaveable { mutableStateOf(value = false) }
    var isUserMode by rememberSaveable { mutableStateOf(value = false) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isAdminLoggedIn || isUserMode) {
            MainContainerScreen(
                isAdmin = isAdminLoggedIn,
                currentUser = loggedInUser,
                onSwitchMode = {
                    isAdminLoggedIn = false
                    isUserMode = false
                    loggedInUser = null
                }
            )
        } else {
            LoginScreen(
                onAdminLogin = { username, password ->
                    val user = dbHelper.authenticateUser(username, password)
                    if (user != null) {
                        loggedInUser = user
                        isAdminLoggedIn = true
                        null
                    } else {
                        "Invalid username or password."
                    }
                },
                onRegisterUser = { name, username, password ->
                    val newUser = User(name, username, password, "ADMIN")
                    val success = dbHelper.registerUser(newUser)
                    if (success) {
                        null
                    } else {
                        "Username '$username' is already registered."
                    }
                },
                onContinueAsUser = {
                    isUserMode = true
                }
            )
        }
    }
}

@Composable
private fun LoginScreen(
    onAdminLogin: (String, String) -> String?,
    onRegisterUser: (String, String, String) -> String?,
    onContinueAsUser: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var regName by rememberSaveable { mutableStateOf("") }
    var regUsername by rememberSaveable { mutableStateOf("") }
    var regPassword by rememberSaveable { mutableStateOf("") }
    var regConfirmPassword by rememberSaveable { mutableStateOf("") }
    var regErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var regSuccessMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.padding(bottom = 12.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🗺️", style = MaterialTheme.typography.headlineLarge)
                }
            }

            Text(
                "Nasugbu Retail & Coverage Mapper",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "📍 Municipality of Nasugbu, Batangas",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        @Suppress("DEPRECATION")
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("🔑 Login", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("📝 Register", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text("📱 Public", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    when (selectedTab) {
                        0 -> {
                            Text(
                                "Account Sign In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Enter your username and password to log in.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            regSuccessMessage?.let {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "✅ $it",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    errorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Username") },
                                leadingIcon = { Text("👤") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(10.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    errorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Password") },
                                leadingIcon = { Text("🔒") },
                                trailingIcon = {
                                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Text(if (passwordVisible) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp)
                            )

                            errorMessage?.let {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        "⚠️ $it",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = onAdminLogin(username.trim(), password)
                                    isLoading = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Logging in...", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Log In", modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .clickable {
                                        username = "admin"
                                        password = "admin123"
                                        errorMessage = null
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    "💡 Auto-fill demo admin credentials (admin / admin123)",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        1 -> {
                            Text(
                                "Create Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Register your account to access branch management.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regName,
                                onValueChange = {
                                    regName = it
                                    regErrorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Full Name (e.g. Juan Dela Cruz)") },
                                leadingIcon = { Text("🏷️") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = {
                                    regUsername = it
                                    regErrorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Username") },
                                leadingIcon = { Text("👤") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = {
                                    regPassword = it
                                    regErrorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Password") },
                                leadingIcon = { Text("🔒") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = {
                                    regConfirmPassword = it
                                    regErrorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Confirm Password") },
                                leadingIcon = { Text("🔑") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp)
                            )

                            regErrorMessage?.let {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        "⚠️ $it",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    when {
                                        regName.isBlank() -> regErrorMessage = "Please enter your Full Name."
                                        regUsername.isBlank() -> regErrorMessage = "Please choose a Username."
                                        regPassword.isBlank() -> regErrorMessage = "Please enter a Password."
                                        regPassword != regConfirmPassword -> regErrorMessage = "Passwords do not match."
                                        else -> {
                                            isLoading = true
                                            val err = onRegisterUser(regName.trim(), regUsername.trim(), regPassword)
                                            isLoading = false
                                            if (err == null) {
                                                regSuccessMessage = "Account created for '$regName'! You can now log in."
                                                username = regUsername.trim()
                                                password = regPassword
                                                selectedTab = 0
                                            } else {
                                                regErrorMessage = err
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Creating account...", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Save Account", modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Mobile App Public Directory",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Open the mobile coverage app to browse retail branches & check coverage areas in Nasugbu without logging in.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(Modifier.height(4.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("✨ Mobile App Highlights:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                        Text("🗺️ View Nasugbu Municipality Boundary Polygon", style = MaterialTheme.typography.bodySmall)
                                        Text("🏪 Search branches by name, barangay, or category", style = MaterialTheme.typography.bodySmall)
                                        Text("🔵 Check branch delivery & service radius circles", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = onContinueAsUser,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("🚀 Launch Mobile App Mode", modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Retail Branch & Service Coverage Mapper",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContainerScreen(
    isAdmin: Boolean,
    currentUser: User? = null,
    onSwitchMode: () -> Unit
) {
    val context = LocalContext.current
    val branches = remember {
        mutableStateListOf<Branch>().apply {
            addAll(BranchRepository.getAllBranches(context))
        }
    }

    fun refreshBranches() {
        branches.clear()
        branches.addAll(BranchRepository.getAllBranches(context))
    }

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD.name) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var focusedBranchOnMap by remember { mutableStateOf<Branch?>(null) }

    var editingBranch by remember { mutableStateOf<Branch?>(null) }
    var isAddingBranch by remember { mutableStateOf(false) }
    var deletingBranch by remember { mutableStateOf<Branch?>(null) }

    val activeTab = AppTab.valueOf(selectedTab)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Nasugbu Coverage Mapper",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isAdmin) "🔑 User: ${currentUser?.name ?: "System Administrator"}" else "📱 Mobile Customer Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSwitchMode) {
                        Text(if (isAdmin) "Log Out" else "Admin Access")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { selectedTab = tab.name },
                        icon = { Text(tab.badgeText) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (activeTab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    branches = branches,
                    isAdmin = isAdmin,
                    onManageBranches = { selectedTab = AppTab.BRANCHES.name },
                    onViewMap = { selectedTab = AppTab.MAP.name },
                    onAddBranch = { isAddingBranch = true }
                )
                AppTab.BRANCHES -> BranchesScreen(
                    branches = branches,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    isAdmin = isAdmin,
                    onSearchQueryChange = { searchQuery = it },
                    onCategorySelect = { selectedCategory = it },
                    onAddBranch = { isAddingBranch = true },
                    onEditBranch = { editingBranch = it },
                    onDeleteBranch = { deletingBranch = it },
                    onViewOnMap = { branch ->
                        focusedBranchOnMap = branch
                        selectedTab = AppTab.MAP.name
                    }
                )
                AppTab.MAP -> CoverageMapScreen(
                    branches = branches,
                    focusedBranch = focusedBranchOnMap,
                    onClearFocus = { focusedBranchOnMap = null }
                )
            }
        }
    }

    if (isAddingBranch || (editingBranch != null)) {
        BranchEditorDialog(
            branch = editingBranch,
            onDismiss = {
                isAddingBranch = false
                editingBranch = null
            },
            onSave = { updatedBranch ->
                if (editingBranch != null) {
                    BranchRepository.updateBranch(context, updatedBranch)
                } else {
                    BranchRepository.addBranch(context, updatedBranch)
                }
                refreshBranches()
                isAddingBranch = false
                editingBranch = null
            }
        )
    }

    deletingBranch?.let { branch ->
        AlertDialog(
            onDismissRequest = { deletingBranch = null },
            title = { Text("Delete Branch") },
            text = { Text("Are you sure you want to delete '${branch.name}' from Nasugbu service coverage system?") },
            confirmButton = {
                Button(
                    onClick = {
                        BranchRepository.deleteBranch(context, branch.id)
                        refreshBranches()
                        deletingBranch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingBranch = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "grocery" -> Color(0xFF0288D1)
        "hardware" -> Color(0xFFE65100)
        "pharmacy" -> Color(0xFFD32F2F)
        "service center" -> Color(0xFF7B1FA2)
        "stationery" -> Color(0xFF388E3C)
        "printing shop" -> Color(0xFFF57C00)
        else -> Color(0xFF00796B)
    }
}

fun getCategoryIcon(category: String): String {
    return when (category.lowercase()) {
        "hardware" -> "🛠️"
        "grocery" -> "🛒"
        "pharmacy" -> "🏥"
        "service center" -> "🔧"
        "stationery" -> "✏️"
        "printing shop" -> "🖨️"
        else -> "🏬"
    }
}

fun openGoogleMapsDirections(context: Context, branch: Branch) {
    try {
        val uri = "https://www.google.com/maps/search/?api=1&query=${branch.latitude},${branch.longitude}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open Google Maps", Toast.LENGTH_SHORT).show()
    }
}

fun shareBranchInfo(context: Context, branch: Branch) {
    try {
        val shareText = """
            ${getCategoryIcon(branch.category)} ${branch.name}
            🏷️ Category: ${branch.category}
            📍 Address: ${branch.address}
            📞 Contact: ${branch.contact.ifBlank { "N/A" }}
            ⭕ Coverage Radius: ${branch.coverageRadiusKm} km
            🗺️ Location: https://maps.google.com/?q=${branch.latitude},${branch.longitude}
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share Branch Details"))
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to share branch details", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun DashboardScreen(
    branches: List<Branch>,
    isAdmin: Boolean,
    onManageBranches: () -> Unit,
    onViewMap: () -> Unit,
    onAddBranch: () -> Unit
) {
    val categoryCounts = remember(branches) {
        branches.groupBy { it.category }.mapValues { (_, value) -> value.size }
    }

    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    val dbPath = remember { DatabaseHelper.getDatabasePathString(context) }
    val userCount = remember { dbHelper.userCount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero System Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("System Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ) {
                        Text("🟢 Active System", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Municipality of Nasugbu, Batangas Retail Coverage System", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🏪 Registered Branches", style = MaterialTheme.typography.labelMedium)
                    Text(
                        branches.size.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Located in Nasugbu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("📍 Service Boundary", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "Nasugbu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("100% Limited to Nasugbu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "📊 Category Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIES.filter { it != "All" }.forEach { category ->
                        val count = categoryCounts[category] ?: 0
                        val catColor = getCategoryColor(category)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = catColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category, style = MaterialTheme.typography.labelMedium, color = catColor, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Surface(shape = CircleShape, color = catColor) {
                                    Text(
                                        count.toString(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "💾 Database File Info (.sqlite)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "File Name: NasugbuCoverageMapper.sqlite (${DatabaseHelper.DATABASE_NAME})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Path: $dbPath",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("👥 Registered Accounts: $userCount", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("🏪 Saved Branches: ${branches.size}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val exportedPath = DatabaseHelper.exportDatabaseToDownloads(context)
                        if (exportedPath != null) {
                            Toast.makeText(context, "✅ File Exported to Downloads: $exportedPath", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "⚠️ Saved in app database storage: $dbPath", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📤 Export .sqlite File to Downloads Folder")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "⚡ Quick System Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isAdmin) {
                        Button(
                            onClick = onAddBranch,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Add Branch")
                        }
                    }
                    Button(
                        onClick = onManageBranches,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Search Directory")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onViewMap,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🗺️ View Interactive Nasugbu Coverage Map")
                }
            }
        }

        Text(
            "Recent Branches in Nasugbu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        branches.take(3).forEach { branch ->
            val catColor = getCategoryColor(branch.category)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(branch.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = catColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                branch.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = catColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("📍 ${branch.address}", style = MaterialTheme.typography.bodyMedium)
                    if (branch.contact.isNotBlank()) {
                        Text("📞 ${branch.contact}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun BranchesScreen(
    branches: List<Branch>,
    searchQuery: String,
    selectedCategory: String,
    isAdmin: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onAddBranch: () -> Unit,
    onEditBranch: (Branch) -> Unit,
    onDeleteBranch: (Branch) -> Unit,
    onViewOnMap: (Branch) -> Unit
) {
    val filteredBranches = remember(branches, searchQuery, selectedCategory) {
        branches.filter { branch ->
            val matchesQuery = searchQuery.isBlank() ||
                branch.name.contains(searchQuery, ignoreCase = true) ||
                branch.address.contains(searchQuery, ignoreCase = true) ||
                branch.contact.contains(searchQuery, ignoreCase = true)
            val matchesCategory = (selectedCategory == "All") || branch.category.equals(selectedCategory, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Branch Directory", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Displaying ${filteredBranches.size} branches in Nasugbu", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (isAdmin) {
                Button(
                    onClick = onAddBranch,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Branch")
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search by name, barangay, contact...") },
            leadingIcon = { Text("🔍") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    TextButton(onClick = { onSearchQueryChange("") }) {
                        Text("✕", fontWeight = FontWeight.Bold)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CATEGORIES.forEach { category ->
                val icon = if (category == "All") "🔍" else getCategoryIcon(category)
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelect(category) },
                    label = { Text("$icon $category") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filteredBranches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (branches.isEmpty()) "No branches added yet." else "No matching branches found in Nasugbu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBranches, key = { it.id }) { branch ->
                    val catColor = getCategoryColor(branch.category)
                    val icon = getCategoryIcon(branch.category)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$icon ${branch.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = catColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        branch.category,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = catColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("📍 ${branch.address}", style = MaterialTheme.typography.bodyMedium)
                            if (branch.contact.isNotBlank()) {
                                Text("📞 ${branch.contact}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "⭕ Radius: ${branch.coverageRadiusKm} km | Lat: ${branch.latitude}, Lng: ${branch.longitude}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onViewOnMap(branch) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🗺️ Map View")
                                }
                                OutlinedButton(
                                    onClick = { openGoogleMapsDirections(context, branch) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🧭 Directions")
                                }
                                TextButton(
                                    onClick = { shareBranchInfo(context, branch) }
                                ) {
                                    Text("📤 Share")
                                }
                                if (isAdmin) {
                                    TextButton(onClick = { onEditBranch(branch) }) {
                                        Text("✏️ Edit")
                                    }
                                    TextButton(onClick = { onDeleteBranch(branch) }) {
                                        Text("🗑️ Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun loadNasugbuBoundary(context: Context): List<LatLng> {
    try {
        val json = context.assets
            .open("nasugbu.geojson")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val features = root.optJSONArray("features")
        if (features != null && features.length() > 0) {
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val geometry = feature.optJSONObject("geometry")
                if (geometry != null) {
                    val type = geometry.optString("type")
                    val coordinates = geometry.optJSONArray("coordinates")
                    if (coordinates != null) {
                        if (type.equals("Polygon", ignoreCase = true)) {
                            val outerRing = coordinates.optJSONArray(0)
                            if (outerRing != null && outerRing.length() > 0) {
                                val points = mutableListOf<LatLng>()
                                for (j in 0 until outerRing.length()) {
                                    val pt = outerRing.optJSONArray(j)
                                    if (pt != null && pt.length() >= 2) {
                                        points.add(LatLng(pt.getDouble(1), pt.getDouble(0)))
                                    }
                                }
                                if (points.isNotEmpty()) return points
                            }
                        } else if (type.equals("MultiPolygon", ignoreCase = true)) {
                            val poly = coordinates.optJSONArray(0)
                            if (poly != null) {
                                val outerRing = poly.optJSONArray(0)
                                if (outerRing != null && outerRing.length() > 0) {
                                    val points = mutableListOf<LatLng>()
                                    for (j in 0 until outerRing.length()) {
                                        val pt = outerRing.optJSONArray(j)
                                        if (pt != null && pt.length() >= 2) {
                                            points.add(LatLng(pt.getDouble(1), pt.getDouble(0)))
                                        }
                                    }
                                    if (points.isNotEmpty()) return points
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (_: Exception) {
        // Fallback to default points if asset parsing fails
    }
    return NasugbuBoundary.DEFAULT_POINTS
}

@Composable
private fun CoverageMapScreen(
    branches: List<Branch>,
    focusedBranch: Branch?,
    onClearFocus: () -> Unit
) {
    val context = LocalContext.current
    val nasugbuBoundary = remember { loadNasugbuBoundary(context) }
    var activeBoundaryPoints by remember { mutableStateOf(nasugbuBoundary) }
    var selectedBarangay by remember { mutableStateOf("Nasugbu Center (Poblacion)") }
    var selectedBranchOnMap by remember { mutableStateOf(focusedBranch) }

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(Unit) {
        mapViewRef.value?.onResume()
        onDispose {
            mapViewRef.value?.onPause()
        }
    }

    val centerLat = focusedBranch?.latitude ?: NASUGBU_CENTER.latitude
    val centerLng = focusedBranch?.longitude ?: NASUGBU_CENTER.longitude
    val zoom = if (focusedBranch != null) 16.0 else 13.0

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // Properly configure OSMDroid for OpenStreetMap API v0.6 per OpenStreetMap Tile & API Policy
                val prefs = ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                val config = Configuration.getInstance()
                config.load(ctx, prefs)
                config.userAgentValue = "RetailBusinessAndServiceCoverageMapper/1.0 (Android OS; OpenStreetMap API v0.6; Contact: developer.nasugbumapper@gmail.com)"
                config.osmdroidBasePath = java.io.File(ctx.cacheDir, "osmdroid")
                config.osmdroidTileCache = java.io.File(ctx.cacheDir, "osmdroid/tiles")
                config.expirationExtendedDuration = 86400000L // 24 hours caching

                MapView(ctx).apply {
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(zoom)
                    controller.setCenter(GeoPoint(centerLat, centerLng))
                    
                    // Strictly limit map view and scrolling to Nasugbu municipality bounds
                    minZoomLevel = 11.0
                    maxZoomLevel = 19.0
                    setScrollableAreaLimitDouble(org.osmdroid.util.BoundingBox(14.28, 120.88, 13.90, 120.30))

                    mapViewRef.value = this

                    if (activeBoundaryPoints.isNotEmpty()) {
                        val osmPoints = activeBoundaryPoints.map { GeoPoint(it.latitude, it.longitude) }
                        val polygon = OsmPolygon(this).apply {
                            points = osmPoints
                            fillColor = android.graphics.Color.argb(50, 45, 120, 84)
                            strokeColor = android.graphics.Color.parseColor("#1B5E20")
                            strokeWidth = 6f
                        }
                        overlays.add(polygon)
                    }

                    branches.forEach { branch ->
                        val marker = Marker(this).apply {
                            position = GeoPoint(branch.latitude, branch.longitude)
                            title = branch.name
                            subDescription = "Category: ${branch.category}\nAddress: ${branch.address}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { m, _ ->
                                m.showInfoWindow()
                                selectedBranchOnMap = branch
                                true
                            }
                        }
                        overlays.add(marker)
                    }
                }
            },
            update = { mapView ->
                mapView.controller.setCenter(GeoPoint(centerLat, centerLng))
                mapView.controller.setZoom(zoom)
                mapView.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "🗺️ Nasugbu OpenStreetMap",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Active Points: ${activeBoundaryPoints.size} boundary nodes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("📍 Quick Jump to Barangay in Nasugbu:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(NasugbuBoundary.NASUGBU_BARANGAY_LOCATIONS.keys.toList()) { barangayName ->
                            val barangayCoords = NasugbuBoundary.NASUGBU_BARANGAY_LOCATIONS[barangayName]
                            FilterChip(
                                selected = selectedBarangay == barangayName,
                                onClick = {
                                    selectedBarangay = barangayName
                                    barangayCoords?.let { targetLatLng ->
                                        mapViewRef.value?.controller?.animateTo(GeoPoint(targetLatLng.latitude, targetLatLng.longitude))
                                        mapViewRef.value?.controller?.setZoom(15.0)
                                    }
                                },
                                label = { Text(barangayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (selectedBranchOnMap != null) 210.dp else 24.dp, end = 16.dp)
                .clickable {
                    mapViewRef.value?.controller?.animateTo(GeoPoint(NASUGBU_CENTER.latitude, NASUGBU_CENTER.longitude))
                    mapViewRef.value?.controller?.setZoom(13.0)
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(6.dp))
                Text("Nasugbu Center", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }

        // OSM Attribution footer as required by Tile Usage Policy
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = if (selectedBranchOnMap != null) 210.dp else 24.dp, start = 16.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 2.dp
        ) {
            Text(
                "© OpenStreetMap contributors",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        selectedBranchOnMap?.let { branch ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            branch.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                selectedBranchOnMap = null
                                onClearFocus()
                            }
                        ) {
                            Text("Close")
                        }
                    }
                    Text("Category: ${branch.category}", style = MaterialTheme.typography.bodyMedium)
                    Text("Address: ${branch.address}", style = MaterialTheme.typography.bodyMedium)
                    if (branch.contact.isNotBlank()) {
                        Text("Contact: ${branch.contact}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        "Coverage Radius: ${branch.coverageRadiusKm} km | Lat: ${branch.latitude}, Lng: ${branch.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            mapViewRef.value?.controller?.animateTo(GeoPoint(branch.latitude, branch.longitude))
                            mapViewRef.value?.controller?.setZoom(16.0)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Center Map on ${branch.name}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchEditorDialog(
    branch: Branch?,
    onDismiss: () -> Unit,
    onSave: (Branch) -> Unit
) {
    var name by remember(branch?.id) { mutableStateOf(branch?.name.orEmpty()) }
    var category by remember(branch?.id) { mutableStateOf(branch?.category ?: "General Retail") }
    var address by remember(branch?.id) { mutableStateOf(branch?.address.orEmpty()) }
    var contact by remember(branch?.id) { mutableStateOf(branch?.contact.orEmpty()) }
    var latitude by remember(branch?.id) {
        mutableStateOf(branch?.latitude?.toString() ?: NASUGBU_CENTER.latitude.toString())
    }
    var longitude by remember(branch?.id) {
        mutableStateOf(branch?.longitude?.toString() ?: NASUGBU_CENTER.longitude.toString())
    }
    var coverageRadius by remember(branch?.id) {
        mutableStateOf(branch?.coverageRadiusKm?.toString() ?: "2.5")
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var barangayExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember(branch?.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (branch == null) "Add New Branch in Nasugbu" else "Edit Branch Information") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Branch Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        CATEGORIES.filter { it != "All" }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = barangayExpanded,
                    onExpandedChange = { barangayExpanded = !barangayExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            barangayExpanded = true
                        },
                        label = { Text("Barangay / Address in Nasugbu") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = barangayExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = barangayExpanded,
                        onDismissRequest = { barangayExpanded = false }
                    ) {
                        NasugbuBoundary.NASUGBU_BARANGAYS
                            .asSequence()
                            .filter { it.contains(address, ignoreCase = true) }
                            .take(5)
                            .forEach { barangay ->
                                DropdownMenuItem(
                                    text = { Text(barangay) },
                                    onClick = {
                                        address = "$barangay, Nasugbu, Batangas"
                                        barangayExpanded = false
                                    }
                                )
                            }
                    }
                }

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = coverageRadius,
                    onValueChange = { coverageRadius = it },
                    label = { Text("Coverage Radius (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedLat = latitude.toDoubleOrNull()
                    val parsedLng = longitude.toDoubleOrNull()
                    val parsedRadius = coverageRadius.toDoubleOrNull()

                    when {
                        name.isBlank() -> errorMessage = "Branch name is required."
                        address.isBlank() -> errorMessage = "Address or barangay is required."
                        parsedLat == null || parsedLng == null -> errorMessage = "Enter valid latitude and longitude numbers."
                        parsedRadius == null || parsedRadius <= 0 -> errorMessage = "Enter a valid coverage radius in km."
                        !NasugbuBoundary.contains(parsedLat, parsedLng) ->
                            errorMessage = "⚠️ Location is OUTSIDE Nasugbu! The coverage area is strictly limited to Nasugbu, Batangas."
                        else -> {
                            val newId = branch?.id ?: System.currentTimeMillis()
                            onSave(
                                Branch(
                                    newId,
                                    name.trim(),
                                    address.trim(),
                                    contact.trim(),
                                    category,
                                    parsedLat,
                                    parsedLng,
                                    parsedRadius
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Save Branch")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
