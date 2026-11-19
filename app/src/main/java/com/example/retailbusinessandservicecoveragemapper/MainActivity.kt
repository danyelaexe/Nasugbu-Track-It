package com.example.retailbusinessandservicecoveragemapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.retailbusinessandservicecoveragemapper.ui.theme.RetailBusinessAndServiceCoverageMapperTheme

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

@Composable
fun RetailBusinessAndServiceCoverageMapperApp() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }

    var isLoggedIn by rememberSaveable { mutableStateOf(value = false) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isLoggedIn) {
            MainContainerScreen(
                isAdmin = true,
                currentUser = loggedInUser,
                onSwitchMode = {
                    isLoggedIn = false
                    loggedInUser = null
                }
            )
        } else {
            LoginScreen(
                onAdminLogin = { username: String, password: String ->
                    val user = dbHelper.authenticateUser(username, password)
                    if (user != null) {
                        loggedInUser = user
                        isLoggedIn = true
                        null
                    } else {
                        "Invalid username or password."
                    }
                },
                onRegisterUser = { name: String, username: String, password: String ->
                    val newUser = User(name, username, password, "ADMIN")
                    val success = dbHelper.registerUser(newUser)
                    if (success) {
                        null
                    } else {
                        "Username '$username' is already registered."
                    }
                },
                onResetPassword = { username: String, newPassword: String ->
                    val success = dbHelper.resetPassword(username, newPassword)
                    if (success) null else "Failed to update password."
                },
                onGetUserEmail = { identifier: String ->
                    dbHelper.getUserEmail(identifier)
                }
            )
        }
    }
}
