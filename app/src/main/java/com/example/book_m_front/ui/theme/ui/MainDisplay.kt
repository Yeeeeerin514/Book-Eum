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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.R
import com.example.book_m_front.ui.theme.ui.book.BookRow
import com.example.book_m_front.ui.theme.ui_resource.AppColors

/*
data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val progress: Int,
    val tags: List<String>
)*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDisplayScreen(
    onUserClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onSearchButtonClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // 샘플 데이터
    //TODO 이거 api에게 받아와서 저장하도록 해야됨.
    val historyBooks = getHistoryBooks()
    /*val historyBooks = List(4) { index ->
        Book(
            id = index,
            title = "책 제목 ${index + 1}",
            author = "작은 글 설명",
            progress = if (index == 0) 0 else 0,
            tags = listOf("장르", "키워드", "키워드")
        )
    }

    val myHistory = List(3) { index ->
        Book(
            id = index + 10,
            title = "책 제목 ${index + 1}",
            author = "작은 글 설명",
            progress = 22,
            tags = listOf()
        )
    }*/

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
                    IconButton(onClick = {onUserClick()}) {
                        Icon(Icons.Default.Person, contentDescription = "프로필")
                    }
                    IconButton(onClick = { /*TODO 나중에 구현한다면*/}) {
                        Icon(Icons.Default.ThumbUp, contentDescription = "북마크")
                    }
                    IconButton(onClick = {/*TODO 나중에 구현한다면*/}) {
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
                    placeholder = { Text("책 제목으로 검색") },
                    trailingIcon = {
                        IconButton(onClick = {onSearchButtonClick(searchQuery)}){
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

            //lazy column안에서는 모두 item{}으로 씌워야 하나보다.
            item {Spacer(modifier = Modifier.height(20.dp))}

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
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(historyBooks) { book ->
                        InProgressBookCard(
                            book = book,
                            showProgress = book.id == 0
                        )
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
                BookRow(
                    books = historyBooks,
                    onBookClick = { book ->
                        // TODO: 다운로드 로직 추가 필요
                        // 임시로 ISBN만 전달
                        onBookClick(book.isbn)
                    }
                )
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
/*

@Composable
fun InProgressBookCard(
    book: Book,
    showProgress: Boolean = false
) {
    Column (
        modifier = Modifier
            .width(120.dp)
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            // 상단 완료 버튼 또는 진행률
            if (showProgress && book.progress > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-15).dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.DeepGreen.copy(alpha = 0.7f)
                ) {
                    Text(
                        "  진행률 ${book.progress}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 15.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.DeepGreen
                ) {
                    Text(
                        "장르  ",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // 하단 정보
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 태그들
                if (book.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        book.tags.take(2).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.DeepGreen.copy(alpha =0.7f)
                            ) {
                                Text(
                                    tag,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp
                                    )
                                )
                            }
                        }
                    }
                    if (book.tags.size > 2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            book.tags.drop(2).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppColors.DeepGreen.copy(alpha =0.7f)
                                ) {
                                    Text(
                                        tag,
                                        color = Color.White,
                                        fontSize = 9.sp,
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
        }
        Text(
            book.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            book.author,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}
*/

@Preview
@Composable
fun MainDisplayPreview(){
    MainDisplayScreen()
}