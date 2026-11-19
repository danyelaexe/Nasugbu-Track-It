package com.example.retailbusinessandservicecoveragemapper

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onAdminLogin: (String, String) -> String?,
    onRegisterUser: (String, String, String) -> String?,
    onResetPassword: (String, String) -> String?,
    onGetUserEmail: (String) -> String?
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var failedLoginAttempts by rememberSaveable { mutableIntStateOf(0) }
    var lockoutEndTime by rememberSaveable { mutableStateOf(0L) }

    var regName by rememberSaveable { mutableStateOf("") }
    var regUsername by rememberSaveable { mutableStateOf("") }
    var regPassword by rememberSaveable { mutableStateOf("") }
    var regConfirmPassword by rememberSaveable { mutableStateOf("") }
    var regErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var regSuccessMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var isForgotPasswordDialogOpen by rememberSaveable { mutableStateOf(false) }

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
                "Track-It Nasugbu",
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

                            TextButton(
                                onClick = { isForgotPasswordDialogOpen = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Forgot Password?", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }

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
                                    val now = System.currentTimeMillis()
                                    if (now < lockoutEndTime) {
                                        val secs = ((lockoutEndTime - now) / 1000).coerceAtLeast(1L)
                                        errorMessage = "⚠️ Too many failed login attempts. Please try again after $secs seconds."
                                    } else {
                                        isLoading = true
                                        val err = onAdminLogin(username.trim(), password)
                                        isLoading = false
                                        if (err != null) {
                                            failedLoginAttempts++
                                            if (failedLoginAttempts >= 5) {
                                                lockoutEndTime = System.currentTimeMillis() + 30000L
                                                errorMessage = "⚠️ 5 failed login attempts reached. Account temporarily locked for 30 seconds."
                                                failedLoginAttempts = 0
                                            } else {
                                                errorMessage = "$err (Attempt $failedLoginAttempts of 5)"
                                            }
                                        } else {
                                            failedLoginAttempts = 0
                                            lockoutEndTime = 0L
                                            errorMessage = null
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
                                    Text("Logging in...", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Log In", modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        else -> {
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
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Track-It Nasugbu - Branch & Coverage Mapper",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (isForgotPasswordDialogOpen) {
        ForgotPasswordDialog(
            onDismiss = { isForgotPasswordDialogOpen = false },
            onGetUserEmail = onGetUserEmail,
            onResetPassword = onResetPassword
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onGetUserEmail: (String) -> String?,
    onResetPassword: (String, String) -> String?
) {
    val captchaChars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789#$@%&*"
    var username by rememberSaveable { mutableStateOf("") }
    var captchaInput by rememberSaveable { mutableStateOf("") }
    var currentCaptcha by rememberSaveable {
        mutableStateOf((1..6).map { captchaChars.random() }.joinToString(""))
    }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var successMessage by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🔑 Reset Password with CAPTCHA") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Enter your username, type the security CAPTCHA code exactly as shown below, and set your new password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("Username") },
                    leadingIcon = { Text("👤") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Security CAPTCHA (Case-Sensitive):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                currentCaptcha,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(
                            onClick = {
                                currentCaptcha = (1..6).map { captchaChars.random() }.joinToString("")
                                captchaInput = ""
                            }
                        ) {
                            Text("🔄 Refresh", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = captchaInput,
                    onValueChange = {
                        captchaInput = it
                        errorMessage = null
                    },
                    label = { Text("Enter CAPTCHA Code") },
                    leadingIcon = { Text("🛡️") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("New Password") },
                    leadingIcon = { Text("🔒") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = {
                        confirmNewPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Password") },
                    leadingIcon = { Text("🔑") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )

                errorMessage?.let {
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

                successMessage?.let {
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
            }
        },
        confirmButton = {
            if (successMessage == null) {
                Button(
                    onClick = {
                        when {
                            username.isBlank() -> errorMessage = "Please enter your username."
                            captchaInput.trim() != currentCaptcha -> {
                                errorMessage = "Incorrect CAPTCHA code. Please try again."
                                currentCaptcha = (1..6).map { captchaChars.random() }.joinToString("")
                                captchaInput = ""
                            }
                            newPassword.isBlank() -> errorMessage = "Please enter a new password."
                            newPassword != confirmNewPassword -> errorMessage = "Passwords do not match."
                            else -> {
                                val email = onGetUserEmail(username.trim())
                                if (email == null) {
                                    errorMessage = "Username not found in the database."
                                } else {
                                    val err = onResetPassword(username.trim(), newPassword)
                                    if (err == null) {
                                        successMessage = "Password reset successfully! You can now log in."
                                        errorMessage = null
                                    } else {
                                        errorMessage = err
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Reset Password")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (successMessage != null) "Close" else "Cancel")
            }
        }
    )
}
