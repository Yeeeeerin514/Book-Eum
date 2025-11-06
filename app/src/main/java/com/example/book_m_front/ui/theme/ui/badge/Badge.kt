package com.example.book_m_front.ui.theme.ui.badge

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//읽는중,완독
@Composable
fun Badge(backgroundColor: Color, text: String) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}