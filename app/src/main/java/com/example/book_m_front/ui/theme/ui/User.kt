package com.example.book_m_front.ui.theme.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.book_m_front.R
import com.example.book_m_front.network.ServerRequestAndResponse.uploadBookToServer
import com.example.book_m_front.network.ServerRequestAndResponse.getFileName
import com.example.book_m_front.network.dto.BookItem
import com.example.book_m_front.network.dto.PlaylistItem
import com.example.book_m_front.network.dto.UserProfileResponse
import com.example.book_m_front.repository.Repository
import com.example.book_m_front.ui.theme.ui.book.BookCard
import com.example.book_m_front.ui.theme.ui.book.handleBookClickToEbookViewer
import com.example.book_m_front.ui.theme.ui.playlist.PlaylistRow
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.launch

/**
 * 사용자 프로필 화면 (API 연동 완료)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateToEbookViewer: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 사용자 정보 상태
    var profile by remember { mutableStateOf<UserProfileResponse?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    // 책 관련 상태
    var likedBooks by remember { mutableStateOf<List<BookItem>>(emptyList()) }
    var uploadedBooks by remember { mutableStateOf<List<BookItem>>(emptyList()) }
    var isLoadingBooks by remember { mutableStateOf(true) }

    // 플레이리스트 상태
    var likedPlaylists by remember { mutableStateOf<List<PlaylistItem>>(emptyList()) }
    var isLoadingPlaylists by remember { mutableStateOf(true) }

    // 책 추가 다이얼로그
    var showAddBookDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    // 데이터 로드
    LaunchedEffect(Unit) {
        val repository = Repository.get()

        // 프로필 로드
        launch {
            repository.getUserProfile().onSuccess {
                profile = it
            }
            isLoadingProfile = false
        }

        // 좋아요한 책 로드
        launch {
            repository.getLikedBooks(page = 0, size = 20).onSuccess {
                likedBooks = it
            }
        }

        // 업로드한 책 로드
        launch {
            repository.getMyUploadedBooks(page = 0, size = 20).onSuccess {
                uploadedBooks = it
            }
            isLoadingBooks = false
        }

        // 좋아요한 플레이리스트 로드
        launch {
            repository.getLikedPlaylists().onSuccess {
                likedPlaylists = it
            }
            isLoadingPlaylists = false
        }
    }

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
                    IconButton(onClick = { /* TODO: 프로필 */ }) {
                        Icon(Icons.Default.Person, "프로필")
                    }
                    IconButton(onClick = { /* TODO: 북마크 */ }) {
                        Icon(Icons.Default.ThumbUp, "북마크")
                    }
                    IconButton(onClick = { /* TODO: 메뉴 */ }) {
                        Icon(Icons.Default.Menu, "메뉴")
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
            if (isLoadingProfile) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.DeepGreen)
                }
            } else if (profile != null) {
                UserProfileSection(profile!!)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 좋아요한 책
            SectionTitle("내가 좋아요한 책")
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoadingBooks) {
                CircularProgressIndicator(
                    color = AppColors.DeepGreen,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (likedBooks.isEmpty()) {
                Text(
                    "좋아요한 책이 없습니다",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    likedBooks.take(4).forEach { book ->
                        BookCard(
                            book = book,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                handleBookClickToEbookViewer(
                                    book = book,
                                    context = context,
                                    coroutineScope = scope,
                                    onStartLoading = { isDownloading = true },
                                    onFinishLoading = { isDownloading = false },
                                    onNavigateToEbookViewer = onNavigateToEbookViewer
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 좋아요한 플레이리스트
            SectionTitle("내가 좋아요한 플레이리스트")
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoadingPlaylists) {
                CircularProgressIndicator(
                    color = AppColors.DeepGreen,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (likedPlaylists.isEmpty()) {
                Text(
                    "좋아요한 플레이리스트가 없습니다",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                PlaylistRow(likedPlaylists, AppColors.DeepGreen)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 추가한 책
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("내가 추가한 책")
                Button(
                    onClick = { showAddBookDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.DeepGreen.copy(alpha = 0.8f)
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

            if (uploadedBooks.isEmpty()) {
                Text(
                    "추가한 책이 없습니다",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uploadedBooks.take(4).forEach { book ->
                        BookCard(
                            book = book,
                            modifier = Modifier.weight(1f),
                            onClick = { /* TODO */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 다운로드 로딩
    if (isDownloading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AppColors.DeepGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("책을 불러오는 중...", fontSize = 16.sp)
                }
            }
        }
    }

    // 책 추가 다이얼로그
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { bookTitle, author, isbn, plot, fileUri ->
                isUploading = true
                scope.launch {
                    try {
                        val result = uploadBookToServer(
                            context = context,
                            title = bookTitle,
                            author = author,
                            isbn = isbn,
                            plot = plot,
                            fileUri = fileUri
                        )

                        if (result.success) {
                            Toast.makeText(
                                context,
                                "책이 성공적으로 추가되었습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            showAddBookDialog = false

                            // 업로드한 책 목록 새로고침
                            Repository.get().getMyUploadedBooks().onSuccess {
                                uploadedBooks = it
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "업로드 실패: ${result.message}",
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
                        isUploading = false
                    }
                }
            },
            darkGreen = AppColors.DeepGreen,
            isUploading = isUploading
        )
    }
}

@Composable
fun UserProfileSection(profile: UserProfileResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        if (!profile.profileImageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = profile.profileImageUrl,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Text(
                text = profile.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            profile.email?.let { email ->
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: 프로필 수정 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.DeepGreen
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
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Uri) -> Unit,
    darkGreen: Color,
    isUploading: Boolean = false
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
        onDismissRequest = { if (!isUploading) onDismiss() },
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
                    singleLine = true,
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("저자") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = plot,
                    onValueChange = { plot = it },
                    label = { Text("줄거리") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.DeepGreen
                    ),
                    onClick = {
                        filePickerLauncher.launch(arrayOf("application/epub+zip"))
                    },
                    enabled = !isUploading
                ) {
                    Text("EPUB 파일 선택하기")
                }

                selectedFileUri?.let {
                    Text(
                        text = "선택된 파일: ${getFileName(LocalContext.current, it)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                if (isUploading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = darkGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("업로드 중...", color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bookTitle.isNotBlank() && author.isNotBlank()
                        && isbn.isNotBlank() && plot.isNotBlank()
                        && selectedFileUri != null) {
                        onConfirm(bookTitle, author, isbn, plot, selectedFileUri!!)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.DeepGreen
                ),
                enabled = !isUploading && bookTitle.isNotBlank()
                        && author.isNotBlank() && isbn.isNotBlank()
                        && plot.isNotBlank() && selectedFileUri != null
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("취소", color = Color.Gray)
            }
        }
    )
}

@Preview
@Composable
fun UserProfilePreview() {
    UserProfileScreen()
}