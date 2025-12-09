package com.example.book_m_front.ui.theme.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sign Up Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("이름을 입력하세요") },
                    singleLine = true,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("비밀번호 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B4332),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            // Phone Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("전화번호", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("010-0000-0000") },
                    singleLine = true,
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
                    when {
                        name.isEmpty() || userId.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() ->
                            errorMessage = "필수 항목을 모두 입력해주세요"
                        password != passwordConfirm ->
                            errorMessage = "비밀번호가 일치하지 않습니다"
                        password.length < 6 ->
                            errorMessage = "비밀번호는 6자 이상이어야 합니다"
                        else -> {
                            if(checkSignUp(name, userId, password, email))  //TODO 여기서 api구현하기. 이미 있는 회원이면 false, 새 회워이면 true를 반환하도록.
                                onSignUpSuccess()
                            else errorMessage = "이미 등록된 회원입니다."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B4332)
                )
            ) {
                Text("회원가입", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}







// ... 기존 SignUpScreen 코드 ...

@Preview(showBackground = true, name = "Default Sign Up Screen")
@Composable
fun SignUpScreenPreview() {
    // 미리보기에서는 클릭 이벤트가 동작할 필요가 없으므로 비워둡니다.
    SignUpScreen(
        onBackClick = {},
        onSignUpSuccess = { _, _, _, _ -> }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Sign Up Screen with Error")
@Composable
fun SignUpScreenErrorPreview() {
    // 비밀번호 불일치 에러 상태를 시뮬레이션합니다.
    var name by remember { mutableStateOf("홍길동") }
    var userId by remember { mutableStateOf("gildong") }
    var password by remember { mutableStateOf("password123") }
    var passwordConfirm by remember { mutableStateOf("password456") } // 다른 비밀번호
    var email by remember { mutableStateOf("gildong@example.com") }
    var phone by remember { mutableStateOf("010-1234-5678") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("회원가입", fontWeight = FontWeight.SemiBold) },
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
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1B4332))
                )
            }

            // User ID Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("아이디 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1B4332))
                )
            }

            // Password Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("비밀번호 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1B4332))
                )
            }

            // Password Confirm Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("비밀번호 확인 *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B4332))
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    modifier = Modifier.fillMaxWidth(),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1B4332))
                )
                // 비밀번호 불일치 에러 메시지 표시
                if (passwordConfirm.isNotEmpty() && password != passwordConfirm) {
                    Text("비밀번호가 일치하지 않습니다", color = Color(0xFFDC2626), fontSize = 12.sp)
                }
            }
            // 나머지 필드들은 미리보기에서 생략하거나 채워 넣을 수 있습니다.
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B4332)
                )
            ) {
                Text("회원가입", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
