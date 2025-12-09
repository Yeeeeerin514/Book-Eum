package com.example.book_m_front.ui.theme.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.network.dto.BookItem
import com.example.book_m_front.repository.Repository
import com.example.book_m_front.ui.theme.ui.book.BookCard
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.launch

/**
 * 책 검색 결과 화면 (API 연동 완료)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchedBookList(
    searchQueryFromNav: String = "",
    onBackClick: () -> Unit = {},
    onBookClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf(searchQueryFromNav) }

    // 검색 결과 상태
    var searchResults by remember { mutableStateOf<List<BookItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 검색 함수
    fun performSearch() {
        if (searchQuery.isEmpty()) {
            Toast.makeText(context, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val repository = Repository.get()
                val result = if (selectedTab == 0) {    //List<BookItem>을 받음.
                    // 제목으로 검색
                    repository.searchBooks(
                        query = searchQuery,
                        limit = 30,
                        offset = 0
                    )
                } else {
                    // 장르로 검색
                    repository.searchBooksByGenre(
                        genre = searchQuery,
                        limit = 30,
                        offset = 0
                    )
                }
                //책 목록 불러오는 걸 성공했을 때
                result.onSuccess { books ->     //books는 List<BookItem>임.
                    searchResults = books       //searchResults 변수에 책 목록을 저장함.
                    if (books.isEmpty()) {
                        Toast.makeText(
                            context,
                            "검색 결과가 없습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                //책 목록을 불러오는 걸 실패했을 때.
                result.onFailure { error ->
                    errorMessage = error.message
                    Toast.makeText(
                        context,
                        error.message ?: "검색 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                errorMessage = "검색 중 오류가 발생했습니다"
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 검색 (Navigation에서 전달받은 검색어)
    LaunchedEffect(searchQueryFromNav) {
        if (searchQueryFromNav.isNotEmpty()) {
            performSearch()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                        Text(
                            "검색 결과",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    onClick = {
                        selectedTab = 0
                        searchResults = emptyList() // 탭 변경 시 결과 초기화
                    },
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
                        selected = selectedTab == 0
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        searchResults = emptyList() // 탭 변경 시 결과 초기화
                    },
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
                        selected = selectedTab == 1
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
            }

            // 검색 바
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
                    IconButton(
                        onClick = { performSearch() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(30.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedBorderColor = AppColors.DeepGreen.copy(alpha = 0.2f),
                    focusedBorderColor = AppColors.DeepGreen.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 검색 결과 또는 로딩/에러 상태
            Box(modifier = Modifier.fillMaxSize()) {
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
                            Text("검색 중...", color = Color.Gray)
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
                                color = Color.Gray
                            )
                        }
                    }
                    searchResults.isEmpty() && searchQuery.isNotEmpty() -> {
                        // 검색 결과 없음
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "검색 결과가 없습니다",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    //검색 결과가 존재하면! (검색 결과를 searchResults 변수에 저장한 상태.
                    searchResults.isNotEmpty() -> {
                        // 검색 결과 그리드
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(searchResults) { book ->
                                BookCard(
                                    book = book,
                                    onClick = { onBookClick(book.isbn) }
                                )
                            }
                        }
                    }
                    else -> {
                        // 초기 상태
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "검색어를 입력하세요",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchedBookListPreview() {
    SearchedBookList(
        searchQueryFromNav = "어린왕자"
    )
}