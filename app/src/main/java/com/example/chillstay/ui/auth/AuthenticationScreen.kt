package com.example.chillstay.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Title
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Let's you in",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Social Login Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Button
            SocialLoginButton(
                text = "Continue with Google",
                onClick = onGoogleClick,
                backgroundColor = Color.White,
                borderColor = Color(0xFFE0E0E0)
            )
            
            // Facebook Button
            SocialLoginButton(
                text = "Continue with Facebook",
                onClick = onFacebookClick,
                backgroundColor = Color.White,
                borderColor = Color(0xFFE0E0E0)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
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
                text = "or",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF757575),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0E0E0)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Sign in with password button
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1AB6B6)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Sign in with password",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Sign up link
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                color = Color(0xFF9E9E9E),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onSignUpClick) {
                Text(
                    text = "Sign up",
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
private fun SocialLoginButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Placeholder for icon
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
