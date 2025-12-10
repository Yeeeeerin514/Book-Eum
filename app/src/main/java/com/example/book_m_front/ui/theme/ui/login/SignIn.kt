package com.example.book_m_front.ui.theme.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
 * 회원가입 화면 (API 연동 완료)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("회원가입", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "정보를 입력해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B4332)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("이름 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("이름을 입력하세요") },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B4332),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // User ID Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("아이디 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = userId,
                    onValueChange = {
                        userId = it
                        errorMessage = ""
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("비밀번호 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("비밀번호를 입력하세요") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
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

            // Password Confirm Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("비밀번호 확인 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("비밀번호를 다시 입력하세요") },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                            Icon(
                                imageVector = if (passwordConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B4332),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
                if (passwordConfirm.isNotEmpty() && password != passwordConfirm) {
                    Text("비밀번호가 일치하지 않습니다", color = Color(0xFFDC2626), fontSize = 12.sp)
                }
            }

            // Email Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("이메일", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("example@email.com") },
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B4332),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // Phone Input (선택적)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("전화번호", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("010-0000-0000") },
                    singleLine = true,
                    enabled = !isLoading,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Sign Up Button
            Button(
                onClick = {
                    // 입력 검증
                    when {
                        name.isEmpty() || userId.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() ->
                            errorMessage = "필수 항목을 모두 입력해주세요"
                        password != passwordConfirm ->
                            errorMessage = "비밀번호가 일치하지 않습니다"
                        password.length < 6 ->
                            errorMessage = "비밀번호는 6자 이상이어야 합니다"
                        else -> {
                            // 회원가입 API 호출
                            isLoading = true
                            errorMessage = ""

                            scope.launch {
                                try {
                                    val repository = Repository.get()
                                    val result = repository.signup(
                                        userId = userId,
                                        password = password,
                                        name = name,
                                        email = email.ifEmpty { null },
                                        phoneNumber = phone.ifEmpty { null }
                                    )

                                    result.onSuccess { authResponse ->
                                        // 회원가입 성공
                                        Toast.makeText(
                                            context,
                                            "${authResponse.user.name}님 회원가입을 환영합니다!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onSignUpSuccess()
                                    }

                                    result.onFailure { error ->
                                        // 회원가입 실패
                                        errorMessage = error.message ?: "회원가입에 실패했습니다"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "회원가입 중 오류가 발생했습니다"
                                } finally {
                                    isLoading = false
                                }
                            }
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
                    Text("회원가입", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onBackClick = {},
        onSignUpSuccess = {}
    )
}