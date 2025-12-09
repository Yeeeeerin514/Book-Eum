package com.example.book_m_front.ui.theme.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.R
import com.example.book_m_front.ui.theme.ui_resource.AppColors

@Composable
fun StartLogin(
    onLoginClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppColors.White)
            .padding(horizontal = 32.dp)
    ) {
        // 로고 텍스트
        Text(
            text = "북-음",
            fontWeight = Bold,
            color = AppColors.DeepGreen,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 로고 이미지
        Image(
            painter = painterResource(R.drawable.deepgreenlogo),
            contentDescription = "앱 로고",
            modifier = Modifier.padding(bottom = 60.dp)
        )

        // 로그인 버튼
        Button(
            onClick = onLoginClick,  // ✅ 수정: 람다 실행이 아닌 함수 참조 전달
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.White
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
        ) {
            Text(
                text = stringResource(R.string.login),
                color = AppColors.DeepGreen,
                fontSize = 16.sp,
                fontWeight = Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 회원가입 버튼
        Button(
            onClick = onSignInClick,  // ✅ 수정: 람다 실행이 아닌 함수 참조 전달
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.DeepGreen
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
        ) {
            Text(
                text = stringResource(R.string.join),
                color = AppColors.White,
                fontSize = 16.sp,
                fontWeight = Bold
            )
        }
    }
}

@Preview
@Composable
fun StartLoginPreview() {
    StartLogin()
}