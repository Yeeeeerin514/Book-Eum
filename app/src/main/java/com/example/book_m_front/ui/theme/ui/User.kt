package com.example.book_m_front.ui.theme.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import com.example.book_m_front.R


class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                UserProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen() {
    val darkGreen = AppColors.DeepGreen
    val scrollState = rememberScrollState()
    var showAddBookDialog by remember { mutableStateOf(false) }

    // 샘플 데이터
    val likedBooks = listOf(
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자")
    )

    val likedPlaylists = listOf(
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자")
    )

    val myBooks = listOf(
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자")
    )

    val recommendedPlaylists = listOf(
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.minilogo),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "북-음",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 프로필 */ }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = { /* 책 */ }) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Book"
                        )
                    }
                    IconButton(onClick = { /* 메뉴 */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 사용자 프로필
            UserProfileSection(darkGreen)

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 좋아요한 책
            SectionTitle("내가 좋아요한 책")
            Spacer(modifier = Modifier.height(12.dp))
            BookRow(likedBooks, darkGreen,
                    onBookClick = { }   //구현 필요
            )//row로 바꾸기

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 좋아요한 플레이리스트
            SectionTitle("내가 좋아요한 플레이리스트")
            Spacer(modifier = Modifier.height(12.dp))
            PlaylistRow(likedPlaylists, darkGreen)

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 추가한 책 (버튼 포함)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("내가 추가한 책")
                Button(
                    onClick = { showAddBookDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkGreen.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "나의 책 추가",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            BookRow(myBooks, darkGreen,
                    onBookClick = {}    //구현 필요
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 제작한 플레이리스트
            SectionTitle("내가 제작한 플레이리스트")
            Spacer(modifier = Modifier.height(12.dp))
            PlaylistRow(recommendedPlaylists, darkGreen)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 책 추가 다이얼로그!!!!!
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { bookTitle, author, isbn, plot ->
                // 책 추가 로직 구현    아마 이 아래에 백엔드로 보내는 함수?
                println("책 추가: $bookTitle by $author")
                showAddBookDialog = false
            },
            darkGreen = darkGreen
        )
    }
}

@Composable
fun UserProfileSection(darkGreen: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Text(
                text = "사용자 이름",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Spotify ID   @id@id@id",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* 프로필 수정 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkGreen
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "프로필 수정",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun BookRow(books: List<BookItem>, darkGreen: Color, onBookClick: (BookItem) -> Unit) {

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books) { book ->
            BookCard(book, darkGreen, onClick = { onBookClick(book)})
        }
    }
}

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
fun PlaylistRow(playlists: List<PlaylistItem>, darkGreen: Color) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(playlists) { playlist ->
            PlaylistCard(playlist, darkGreen)
        }
    }
}

@Composable
fun PlaylistCard(playlist: PlaylistItem, darkGreen: Color) {
    Column(
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
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
            text = playlist.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = playlist.creator,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

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

//'나의 책 추가'누르면 뜨는 화면
@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
    darkGreen: Color
) {
    var bookTitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var plot by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "나의 책 추가",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it },
                    label = { Text("책 제목") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("저자") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = plot,
                    onValueChange = { plot = it },
                    label = { Text("줄거리") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.DeepGreen
                    ),
                    onClick = {
                    // 파일 유형 필터 설정 (예: 모든 파일 or 특정 MIME)
                    filePickerLauncher.launch(arrayOf("application/epub+zip"))
                }) {
                    Text("EPUB 파일 선택하기")
                }

                selectedFileUri?.let {
                    Text("선택된 파일 URI:\n$it")
                }
            }
        },
        //확인 버튼.
        confirmButton = {
            Button(
                onClick = {
                    if (bookTitle.isNotBlank() && author.isNotBlank()
                        &&isbn.isNotBlank() && plot.isNotBlank()
                        && selectedFileUri != null) {
                        onConfirm(bookTitle, author, isbn, plot)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.DeepGreen
                )
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Gray)
            }
        }
    )
}

// 데이터 클래스
data class BookItem(
    val title: String,
    val author: String
)

data class PlaylistItem(
    val title: String,
    val creator: String
)
//data class


@Preview
@Composable
fun UserProfilePreview() {
    UserProfileScreen()
}

