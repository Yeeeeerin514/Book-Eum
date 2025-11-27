package com.example.book_m_front.ui.theme.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview


// Login Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: (String, String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("로그인", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF1B4332)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                Box(modifier = Modifier.size(38.dp).background(Color(0xFF1B4332), CircleShape))
                Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
            }

            Text(
                text = "복 - 음",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B4332)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ID Input
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "아이디",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B4332)
                )
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("아이디를 입력하세요") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B4332),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // Password Input
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "비밀번호",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B4332)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("비밀번호를 입력하세요") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B4332),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFDC2626),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Button
            Button(
                onClick = {
                    if (userId.isEmpty() || password.isEmpty()) {
                        errorMessage = "아이디와 비밀번호를 입력해주세요"
                    } else {
                        onLoginSuccess(userId, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B4332)
                )
            ) {
                Text("로그인", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            // Find ID/Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* TODO */ }) {
                    Text("아이디 찾기", color = Color(0xFF64748B), fontSize = 14.sp)
                }
                Text("  |  ", color = Color(0xFFE0E0E0))
                TextButton(onClick = { /* TODO */ }) {
                    Text("비밀번호 찾기", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        }
    }
}



// ... 기존 LoginScreen 코드 ...

@Preview(showBackground = true, name = "Default Login Screen")
@Composable
fun LoginScreenPreview() {
    // onBackClick과 onLoginSuccess에는 비어있는 람다 함수를 전달하여
    // 미리보기에서는 아무 동작도 하지 않도록 합니다.
    LoginScreen(
        onBackClick = {},
        onLoginSuccess = { _, _ -> }
    )
}








@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Login Screen with Error")
@Composable
fun LoginScreenErrorPreview() {
    // 이 프리뷰에서는 errorMessage가 있는 상태를 시뮬레이션합니다.
    val loginScreenContent: @Composable () -> Unit = {
        var userId by remember { mutableStateOf("testuser") }
        var password by remember { mutableStateOf("") } // 비밀번호는 비워둠
        var passwordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("아이디와 비밀번호를 입력해주세요") } // 에러 메시지 설정

        // 기존 LoginScreen의 UI 구조를 그대로 사용하여
        // 특정 상태(에러 메시지 표시)를 재현합니다.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            TopAppBar(
                title = { Text("로그인", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1B4332)
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                    Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                    Box(modifier = Modifier.size(38.dp).background(Color(0xFF1B4332), CircleShape))
                    Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                    Box(modifier = Modifier.size(30.dp).background(Color(0xFF1B4332), CircleShape))
                }
                Text(
                    text = "복 - 음",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "아이디",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B4332)
                    )
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("아이디를 입력하세요") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1B4332),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "비밀번호",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B4332)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("비밀번호를 입력하세요") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1B4332),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }

                // 에러 메시지를 표시하는 부분
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B4332)
                    )
                ) {
                    Text("로그인", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { /* TODO */ }) {
                        Text("아이디 찾기", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                    Text("  |  ", color = Color(0xFFE0E0E0))
                    TextButton(onClick = { /* TODO */ }) {
                        Text("비밀번호 찾기", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                }
            }
        }
    }
    // 위에서 정의한 Composable을 실행
    loginScreenContent()
}
