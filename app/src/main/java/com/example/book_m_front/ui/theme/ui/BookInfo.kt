package com.example.book_m_front.ui.theme.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.book_m_front.R
import com.example.book_m_front.network.dto.BookInfoResponse
import com.example.book_m_front.repository.Repository
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.launch

/**
 * 책 상세 정보 화면 (API 연동 완료)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfo(
    bookIsbn: String,
    onBackClick: () -> Unit = {},
    onReadClick: (String, String, String) -> Unit = { _, _, _ -> } // title, author, isbn
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 책 정보 상태
    var bookInfo by remember { mutableStateOf<BookInfoResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 좋아요 상태
    var isLiked by remember { mutableStateOf(false) }
    var isLikeLoading by remember { mutableStateOf(false) }

    // 책 정보 로드
    LaunchedEffect(bookIsbn) {
        scope.launch {
            isLoading = true
            errorMessage = null

            try {
                val repository = Repository.get()
                val result = repository.getBookInfo(bookIsbn)

                result.onSuccess { info ->
                    bookInfo = info
                    isLiked = info.isLiked
                }

                result.onFailure { error ->
                    errorMessage = error.message
                }
            } catch (e: Exception) {
                errorMessage = "책 정보를 불러올 수 없습니다"
            } finally {
                isLoading = false
            }
        }
    }

    // 좋아요 토글 함수
    fun toggleLike() {
        isLikeLoading = true
        scope.launch {
            try {
                val repository = Repository.get()
                val result = if (isLiked) {
                    repository.unlikeBook(bookIsbn)
                } else {
                    repository.likeBook(bookIsbn)
                }

                result.onSuccess { liked ->
                    isLiked = liked
                    Toast.makeText(
                        context,
                        if (liked) "좋아요!" else "좋아요 취소",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                result.onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: "오류 발생",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "오류 발생", Toast.LENGTH_SHORT).show()
            } finally {
                isLikeLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.minilogo),
                        contentDescription = null
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        modifier = Modifier.background(color = AppColors.White)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    // 로딩 중
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.DeepGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("책 정보를 불러오는 중...", color = Color.Gray)
                    }
                }
                errorMessage != null -> {
                    // 에러 발생
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            errorMessage ?: "오류 발생",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                bookInfo != null -> {
                    // 책 정보 표시
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 30.dp, vertical = 20.dp)
                    ) {
                        // 책 제목
                        Text(
                            text = bookInfo!!.title,
                            fontSize = 33.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // 책 이미지 및 작가, 출판사
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            // 책 표지
                            if (!bookInfo!!.coverImg.isNullOrEmpty()) {
                                AsyncImage(
                                    model = bookInfo!!.coverImg,
                                    contentDescription = "책 표지",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(150.dp)
                                        .width(100.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .height(150.dp)
                                        .width(100.dp)
                                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(15.dp))

                            Column {
                                // 작가
                                Row(modifier = Modifier.padding(5.dp)) {
                                    Text("작가 | ", fontWeight = FontWeight.Bold)
                                    Text(bookInfo!!.author)
                                }

                                // 출판사
                                if (!bookInfo!!.publisher.isNullOrEmpty()) {
                                    Row(modifier = Modifier.padding(5.dp)) {
                                        Text("출판사 | ", fontWeight = FontWeight.Bold)
                                        Text(bookInfo!!.publisher!!)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 키워드
                                if (!bookInfo!!.keywords.isNullOrEmpty()) {
                                    bookInfo!!.keywords!!.take(3).forEach { keyword ->
                                        Keyword(keyword)
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }

                        // 줄거리
                        if (!bookInfo!!.plot.isNullOrEmpty()) {
                            Column(modifier = Modifier.padding(vertical = 20.dp)) {
                                Text(
                                    text = "줄거리",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                                Text(text = bookInfo!!.plot!!)
                            }
                        }

                        // 목차
                        if (!bookInfo!!.tableOfContents.isNullOrEmpty()) {
                            Column(modifier = Modifier.padding(vertical = 20.dp)) {
                                Text(
                                    text = "목차",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                                Text(text = bookInfo!!.tableOfContents!!)
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp)) // 하단 버튼 공간
                    }
                }
            }

            // 하단 버튼 (좋아요 + 읽기 시작)
            if (bookInfo != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 좋아요 버튼
                        OutlinedButton(
                            onClick = { toggleLike() },
                            enabled = !isLikeLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isLiked)
                                    AppColors.DeepGreen.copy(alpha = 0.1f)
                                else
                                    Color.White
                            )
                        ) {
                            Icon(
                                if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "좋아요",
                                tint = if (isLiked) AppColors.DeepGreen else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isLiked) "좋아요 취소" else "좋아요",
                                color = if (isLiked) AppColors.DeepGreen else Color.Gray
                            )
                        }

                        // 읽기 시작 버튼
                        Button(
                            onClick = {
                                onReadClick(
                                    bookInfo!!.title,
                                    bookInfo!!.author,
                                    bookInfo!!.isbn
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.DeepGreen
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, "읽기 시작")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("읽기 시작")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Keyword(keyword: String) {
    Surface(
        color = AppColors.DeepGreen.copy(alpha = 0.6f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = "# $keyword",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun BookInfoPreview() {
    BookInfo(bookIsbn = "9788954429429")
}