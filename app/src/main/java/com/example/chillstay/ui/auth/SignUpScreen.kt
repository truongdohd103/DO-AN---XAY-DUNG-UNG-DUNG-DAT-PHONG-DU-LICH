package com.example.chillstay.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    state: AuthState,
    onEvent: (AuthIntent) -> Unit,
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onEvent(AuthIntent.ClearMessage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
        
        // Title
        Text(
            text = "Create your\nAccount",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            lineHeight = 35.sp
        )
        
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = state.errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFC62828),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Form
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Email",
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(AuthIntent.EmailChanged(it)) },
                    placeholder = { Text("Email", color = Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Password field
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Password",
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(AuthIntent.PasswordChanged(it)) },
                    placeholder = { Text("Password", color = Color(0xFF757575)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }
            
             // Confirm Password field
             Column(
                 verticalArrangement = Arrangement.spacedBy(8.dp)
             ) {
                 Text(
                     text = "Confirm password",
                     color = Color(0xFF757575),
                     fontSize = 14.sp
                 )
                 OutlinedTextField(
                     value = state.confirmPassword,
                     onValueChange = { onEvent(AuthIntent.ConfirmPasswordChanged(it)) },
                     placeholder = { Text("Password", color = Color(0xFF757575)) },
                     modifier = Modifier.fillMaxWidth(),
                     visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                     colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = if (state.confirmPassword.isNotBlank() && state.password != state.confirmPassword) Color.Red else Color(0xFFE0E0E0),
                         unfocusedBorderColor = if (state.confirmPassword.isNotBlank() && state.password != state.confirmPassword) Color.Red else Color(0xFFE0E0E0)
                     ),
                     shape = RoundedCornerShape(12.dp),
                     trailingIcon = {
                         TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                             Text(
                                 text = if (confirmPasswordVisible) "Hide" else "Show",
                                 fontSize = 12.sp
                             )
                         }
                     }
                 )
                 // Password mismatch error
                 if (state.confirmPassword.isNotBlank() && state.password != state.confirmPassword) {
                     Text(
                         text = "Passwords do not match",
                         color = Color.Red,
                         fontSize = 12.sp
                     )
                 }
             }
            
            // Password Requirements
            PasswordRequirementsCard(password = state.password)
            
            // Security Info Card
            SecurityInfoCard()
            
             // Sign up button
             val isFormValid = remember(state.email, state.password, state.confirmPassword) {
                 state.email.isNotBlank() &&
                 state.password.length >= 8 &&
                 state.password.any { it.isUpperCase() } &&
                 state.password.any { it.isLowerCase() } &&
                 state.password.any { it.isDigit() } &&
                 state.password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) } &&
                 state.password == state.confirmPassword &&
                 state.confirmPassword.isNotBlank()
             }
             
             Button(
                 onClick = { onEvent(AuthIntent.SignUp) },
                 enabled = isFormValid && !state.isLoading,
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(62.dp),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = if (isFormValid) Color(0xFF1AB6B6) else Color(0xFFBDBDBD)
                 ),
                 shape = RoundedCornerShape(12.dp)
             ) {
                 Text(
                     text = "Sign up",
                     color = Color.White,
                     fontSize = 16.sp,
                     fontWeight = FontWeight.Bold
                 )
             }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1AB6B6))
            }
        }
        
        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0E0E0)
            )
            Text(
                text = "or continue with",
                modifier = Modifier.padding(horizontal = 12.dp),
                color = Color(0xFF757575),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0E0E0)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Social login buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialLoginButton(
                text = "Continue with Google",
                onClick = onGoogleClick
            )
            SocialLoginButton(
                text = "Continue with Facebook",
                onClick = onFacebookClick
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sign in link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                color = Color(0xFF9E9E9E),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onSignInClick) {
                Text(
                    text = "Sign in",
                    color = Color(0xFF1AB6B6),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    }
}

@Composable
private fun PasswordRequirementsCard(password: String) {
    val requirements = remember(password) {
        listOf(
            "At least 8 characters long" to (password.length >= 8),
            "Contains uppercase letter" to password.any { it.isUpperCase() },
            "Contains lowercase letter" to password.any { it.isLowerCase() },
            "Contains number" to password.any { it.isDigit() },
            "Contains special character" to password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Password Requirements",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            
            requirements.forEach { (text, isMet) ->
                PasswordRequirementItem(
                    text = text,
                    isMet = isMet
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirementItem(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    if (isMet) Color(0xFF1AB6B6) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isMet) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Show empty circle border when not met
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }
        Text(
            text = text,
            color = if (isMet) Color(0xFF1AB6B6) else Color(0xFF9E9E9E),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SecurityInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1A1AB6B6)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        Color(0xFF1AB6B6),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "For your security, use a strong password that\nyou don't use for other accounts.",
                color = Color(0xFF424242),
                fontSize = 14.sp,
                lineHeight = 19.6.sp
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Gray, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color(0xFF212121),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
