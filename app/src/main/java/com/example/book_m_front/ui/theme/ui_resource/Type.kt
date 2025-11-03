package com.example.book_m_front.ui.theme.ui_resource

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.book_m_front.R


/*val Pretendard = FontFamily(
    Font(R.font.pretendard, FontWeight.Black),
    Font(R.font.pretendard, FontWeight.ExtraBold),
    Font(R.font.pretendard, FontWeight.SemiBold),
    Font(R.font.pretendard, FontWeight.Bold),
    Font(R.font.pretendard, FontWeight.Medium),
    Font(R.font.pretendard, FontWeight.Normal),
    Font(R.font.pretendard, FontWeight.Light),
    Font(R.font.pretendard, FontWeight.ExtraLight),
    Font(R.font.pretendard, FontWeight.Thin)
)*/

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        //fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

