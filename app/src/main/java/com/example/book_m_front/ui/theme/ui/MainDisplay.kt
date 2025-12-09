package com.example.book_m_front.ui.theme.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.R
import com.example.book_m_front.network.dto.BookItem
import com.example.book_m_front.network.dto.ReadingHistoryItem
import com.example.book_m_front.repository.Repository
import com.example.book_m_front.ui.theme.ui.book.BookCard
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.launch

/**
 * 메인 디스플레이 화면 (API 연동 완료)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDisplayScreen(
    onUserClick: () -> Unit = {},
    onBookClick: (String) -> Unit = {},
    onSearchButtonClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // 데이터 상태
    var todayRecommendations by remember { mutableStateOf<List<BookItem>>(emptyList()) }
    var popularBooks by remember { mutableStateOf<List<BookItem>>(emptyList()) }
    var readingHistory by remember { mutableStateOf<List<ReadingHistoryItem>>(emptyList()) }

    // 로딩 상태
    var isLoadingRecommendations by remember { mutableStateOf(true) }
    var isLoadingPopular by remember { mutableStateOf(true) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        val repository = Repository.get()

        // 병렬로 데이터 로드
        launch {
            repository.getTodayRecommendations(limit = 10).onSuccess {
                todayRecommendations = it
            }
            isLoadingRecommendations = false
        }

        launch {
            repository.getPopularBooks(limit = 10).onSuccess {
                popularBooks = it
            }
            isLoadingPopular = false
        }

        launch {
            repository.getReadingHistory(page = 0, size = 10).onSuccess {
                readingHistory = it
            }
            isLoadingHistory = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.minilogo),
                            contentDescription = null
                        )
                        Text(
                            "북-음",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUserClick) {
                        Icon(Icons.Default.Person, contentDescription = "프로필")
                    }
                    IconButton(onClick = { /* TODO: 북마크 */ }) {
                        Icon(Icons.Default.ThumbUp, contentDescription = "북마크")
                    }
                    IconButton(onClick = { /* TODO: 메뉴 */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "메뉴")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // 탭 버튼들
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("책 제목으로 검색") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.DeepGreen.copy(alpha = 0.8f),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("책 장르로 검색") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.DeepGreen.copy(alpha = 0.8f),
                            selectedLabelColor = Color.White,
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                }
            }

            // 검색 바
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (selectedTab == 0) "책 제목으로 검색"
                            else "장르로 검색"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotEmpty()) {
                                onSearchButtonClick(searchQuery)
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "검색")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = AppColors.DeepGreen.copy(alpha = 0.2f),
                    ),
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // 오늘의 책&플리 추천
            item {
                Text(
                    "오늘의 책&플리 추천",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            item {
                if (isLoadingRecommendations) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.DeepGreen)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(todayRecommendations) { book ->
                            BookCard(
                                book = book,
                                onClick = { onBookClick(book.isbn) }
                            )
                        }
                    }
                }
            }

            // 인기 책
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "인기 책",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            item {
                if (isLoadingPopular) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.DeepGreen)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(popularBooks) { book ->
                            BookCard(
                                book = book,
                                onClick = { onBookClick(book.isbn) }
                            )
                        }
                    }
                }
            }

            // 나의 히스토리
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "나의 히스토리",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }

            item {
                if (isLoadingHistory) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.DeepGreen)
                    }
                } else if (readingHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "읽은 책이 없습니다",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(readingHistory) { historyItem ->
                            BookCard(
                                book = historyItem.book,
                                onClick = { onBookClick(historyItem.book.isbn) }
                            )
                        }
                    }
                }
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
fun MainDisplayPreview() {
    MainDisplayScreen()
}