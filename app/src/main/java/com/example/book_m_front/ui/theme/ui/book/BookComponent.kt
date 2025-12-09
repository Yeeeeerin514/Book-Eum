package com.example.book_m_front.ui.theme.ui.book

import android.content.Context
import android.widget.Toast
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
import com.example.book_m_front.network.ServerRequestAndResponse.downloadBookFromServer
import com.example.book_m_front.network.dto.BookItem
import com.example.book_m_front.ui.theme.ui.badge.Badge
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun BookCard(
    book: BookItem,
    modifier: Modifier = Modifier,
    onClick: (BookItem) -> Unit
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable { onClick(book) }
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
                Badge(AppColors.DeepGreen, "읽는중")
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
fun BookRow(books: List<BookItem>, onBookClick: (BookItem) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books) { book ->
            BookCard(book, onClick = onBookClick)
        }
    }
}


/**
 * 책 클릭 시 다운로드 후 E-book 뷰어로 이동하는 헬퍼 함수
 * User.kt에서 사용
 */
fun handleBookClickToEbookViewer(
    book: BookItem,
    context: Context,
    coroutineScope: CoroutineScope,
    onStartLoading: () -> Unit,
    onFinishLoading: () -> Unit,
    onNavigateToEbookViewer: (title: String, author: String, isbn: String, filePath: String) -> Unit
) {
    // 책을 클릭 시 실행할 것 (책 다운로드해서 -> 이북뷰어로 화면 전환)
    onStartLoading()    // -> isDownloading이 true가 됨.

    coroutineScope.launch {
        try {
            // 서버에서 책 다운로드
            val bookFile = downloadBookFromServer(
                context = context,
                isbn = book.isbn
            )

            // 서버에 책이 존재한다면
            if (bookFile != null) {
                // EbookViewerWithMusicScreen으로 이동 : Navigation
                onNavigateToEbookViewer(
                    book.title,
                    book.author,
                    book.isbn,
                    bookFile.absolutePath
                )
            } else {
                // 서버에 책이 존재하지 않으면
                Toast.makeText(
                    context,
                    "책을 불러올 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "오류 발생: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            onFinishLoading()
        }
    }
}