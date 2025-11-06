package com.example.book_m_front.ui.theme.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.ui.theme.ui.BookCard
import com.example.book_m_front.ui.theme.ui.badge.Badge

//데이터 패키지로 따로 관리하는 게 좋을 수도
data class BookItem(//줄거리도 필요하지 않나?
    val title: String,
    val author: String,
    val isbn : String
)
@Composable
fun BookCard(book: BookItem, darkGreen: Color, modifier: Modifier = Modifier
             ,onClick: () -> Unit) {
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Badge(darkGreen, "읽는중")
                Spacer(modifier = Modifier.height(4.dp))
                Badge(Color.Gray, "완독")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = book.author,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}


@Composable
fun BookRow(books: List<BookItem>, darkGreen: Color, onBookClick: (BookItem) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books) { book ->
            BookCard(book, darkGreen, onClick = { onBookClick(book) })
        }
    }
}