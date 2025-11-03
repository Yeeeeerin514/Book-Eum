package com.example.book_m_front.ui.theme.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
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
fun StartLogin(){

    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppColors.White)
    ){
        Text(
            text = "북 - 음",
            fontWeight = Bold,
            color = AppColors.DeepGreen,
            fontSize = 25.sp,
            modifier = Modifier
                .padding(10.dp)
        )
        Image(
            painter = painterResource(R. drawable.deepgreenlogo),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp,
                pressedElevation = 5.dp
            ),
        ){
            Text(
                text = stringResource(R.string.login),
                color = AppColors.DeepGreen)
        }
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.DeepGreen
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp,
                pressedElevation = 5.dp
            ),
        ){
            Text(text=stringResource(R.string.join))
        }
    }
}

@Preview
@Composable
fun StartLoginPreview(){
    StartLogin()
}