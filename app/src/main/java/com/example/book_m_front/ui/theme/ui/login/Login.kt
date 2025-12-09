package com.example.book_m_front.ui.theme.ui.login

import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.repository.Repository
import kotlinx.coroutines.launch

/**
 * 로그인 화면 (API 연동 완료)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                text = "북 - 음",
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
                    onValueChange = {
                        userId = it
                        errorMessage = "" // 입력 시 에러 메시지 초기화
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("아이디를 입력하세요") },
                    singleLine = true,
                    enabled = !isLoading,
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
                    onValueChange = {
                        password = it
                        errorMessage = "" // 입력 시 에러 메시지 초기화
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("비밀번호를 입력하세요") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "비밀번호 숨기기"
                                else
                                    "비밀번호 보기"
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
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Button
            Button(
                onClick = {
                    // 입력 검증
                    if (userId.isEmpty() || password.isEmpty()) {
                        errorMessage = "아이디와 비밀번호를 입력해주세요"
                        return@Button
                    }

                    // 로그인 API 호출
                    isLoading = true
                    errorMessage = ""

                    //UI 테스트용 아이디&비밀번호----
                    when{
                        userId == "test" && password == "test" -> {
                            // 로그인 성공
                            Toast.makeText(
                                context,
                                "test님 환영합니다!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onLoginSuccess()
                        }

                    }
                    //--------------------------

                    scope.launch {
                        try {
                            val repository = Repository.get()
                            val result = repository.login(userId, password)
                            //reposoitory에서, api불러서, userId랑 password보냄.

                            result.onSuccess { authResponse ->
                                // 로그인 성공
                                Toast.makeText(
                                    context,
                                    "${authResponse.user.name}님 환영합니다!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginSuccess()
                            }

                            result.onFailure { error ->
                                // 로그인 실패
                                errorMessage = error.message ?: "로그인에 실패했습니다"
                            }
                        } catch (e: Exception) {
                            errorMessage = "로그인 중 오류가 발생했습니다"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B4332),
                    disabledContainerColor = Color(0xFF1B4332).copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "로그인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Find ID/Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* TODO: 아이디 찾기 */ }) {
                    Text("아이디 찾기", color = Color(0xFF64748B), fontSize = 14.sp)
                }
                Text("  |  ", color = Color(0xFFE0E0E0))
                TextButton(onClick = { /* TODO: 비밀번호 찾기 */ }) {
                    Text("비밀번호 찾기", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onBackClick = {},
        onLoginSuccess = {}
    )
}