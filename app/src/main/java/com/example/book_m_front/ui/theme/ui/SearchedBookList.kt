package com.example.book_m_front.ui.theme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.ui.theme.ui_resource.AppColors

data class SearchBook(
    val id: Int,
    val title: String,
    val subtitle: String,
    val genre: String,
    val tags: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchedBookList(
    searchQueryFromNav: String,      // Navigation에서 전달받은 검색어
    onBookClick: (String) -> Unit,     // 책 클릭 이벤트 (isbn 전달)
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf(searchQueryFromNav) }

    // 샘플 데이터
    val searchResults = List(12) { index ->
        SearchBook(
            id = index,
            title = "책 제목",
            subtitle = "작은설명\n책 큰 설명",
            genre = "장르",
            tags = listOf("#키워드", "#키워드", "#키워드", "#키워드")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 중앙 로고
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.DeepGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            // 로고 대체 아이콘
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // 탭 버튼들
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("책 제목으로 검색") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.DeepGreen.copy(alpha = 0.8f),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = AppColors.DeepGreen.copy(alpha = 0.3f),
                        selectedBorderColor = AppColors.DeepGreen.copy(alpha = 0.8f),
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = true,
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
                        containerColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = AppColors.DeepGreen.copy(alpha = 0.3f),
                        selectedBorderColor = AppColors.DeepGreen.copy(alpha = 0.8f),
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = true
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
            }

            // 검색 바
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("책 제목으로 검색") },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedBorderColor = AppColors.DeepGreen.copy(alpha = 0.2f),
                    focusedBorderColor = AppColors.DeepGreen.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 그리드 검색 결과
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { book ->
                    SearchBookCard(book = book)
                }
            }
        }
    }
}

@Composable
fun SearchBookCard(book: SearchBook) {
    Column(
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            // 상단 장르 라벨
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-4).dp),
                shape = RoundedCornerShape(12.dp),
                color = AppColors.DeepGreen
            ) {
                Text(
                    book.genre,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // 하단 태그들
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 첫 번째 줄 (2개)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    book.tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppColors.DeepGreen.copy(alpha = 0.7f)
                        ) {
                            Text(
                                tag,
                                color = Color.White,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }

                // 두 번째 줄 (나머지)
                if (book.tags.size > 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        book.tags.drop(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AppColors.DeepGreen.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    tag,
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 책 제목
        Text(
            book.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // 책 설명
        Text(
            book.subtitle,
            fontSize = 9.sp,
            color = Color.Gray,
            lineHeight = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookSearchScreenPreview() {
    SearchedBookList()
}