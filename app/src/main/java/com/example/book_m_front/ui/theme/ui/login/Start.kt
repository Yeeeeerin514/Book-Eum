package com.example.book_m_front.ui.theme.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.R
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.delay

private const val SPLASH_DURATION = 2000L
@Composable
fun Start(
    onTimeout: () -> Unit
){
    // LaunchedEffect: Composable이 화면에 나타날 때 (Key가 변경될 때) 코루틴을 실행합니다.
    // Key를 'true'로 설정하여 이 Composable이 처음 시작될 때 단 한 번만 실행되도록 보장합니다.
    LaunchedEffect(key1 = true) {
        // 1. 3초 동안 지연 (화면이 보여지는 시간)
        delay(SPLASH_DURATION)

        // 2. 지연 시간이 지나면 다음 화면으로 이동 요청
        onTimeout()
    }

    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppColors.DeepGreen)
    ){
        Text(
            text = "북 - 음",
            fontWeight = Bold,
            color = AppColors.White,
            fontSize = 25.sp,
            modifier = Modifier
                .padding(10.dp)
        )
        Image(
            painter = painterResource(R.drawable.whitelogo),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
fun StartPreview(){
    Start()
}