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
import androidx.lifecycle.viewModelScope        //백과의 소통을 위해 추가
import com.example.book_m_front.network.Api
import kotlinx.coroutines.launch

// 7. 필요한 import 추가
import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.book_m_front.network.BookUploadResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    //네비게이션 : user창 -> 이북 뷰어 창
    onNavigateToEbookViewer: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val darkGreen = AppColors.DeepGreen //나중에 삭제해도 될듯. 다 그냥 바로 App~으로 하고.
    val scrollState = rememberScrollState()
    var showAddBookDialog by remember { mutableStateOf(false) }

    //사용자 휴대폰에서 데이터 가져오고/저장하기 위한 얘.
    val context = LocalContext.current
    //Context : 안드로이드 앱이 실행되고 있는 현재 상태와 환경에 대한 모든 정보에 접근할 수 있는 "만능 리모컨" 또는 "연결 통로"
    //ㄴ 안드로이드 OS의 기능(파일 읽기/쓰기, Toast 메시지, 리소스 접근 등)
    //LocalContext.current : 현재 UI가 속한 context를 말함. (보통 현재 Activity래)
    //얘로 Uri에 해당하는 파일의 실제 데이터를 읽어오는 기능을 수행 -> 이를 백엔드 서버에 저장
    //서버에서 가져온 책 파일을, 휴대폰에 저장하는 기능을 수행.
    val coroutineScope = rememberCoroutineScope()   //코루틴. 비동기 실행을 위함.

    //파일 보내기(업로드), 가져오기(다운로드) 상태 변수
    var isUploading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }


    //내가 추가한 책 목록
    var myBooks by remember { mutableStateOf(listOf<BookItem>()) }
    //++ 이 후 다른 목록(내가 좋아요 누른 책, 플리..)들도 이렇게 관리 필요함.

    // 앱 시작 시 서버에서 내가 추가한 책 목록 불러오기 (선택사항) => 지금은 안할듯??
    LaunchedEffect(Unit) {
        // TODO: 서버에서 사용자의 책 목록을 불러오는 API 호출
        // myBooks = loadMyBooksFromServer()
    }
    //LaunchedEffect : 네트워크 요청, 데이터베이스 조회, 애니메이션 실행과 같이 Composable 함수의 일반적인 실행 흐름과 다른 생명주기를 갖는 작업을 처리할 때 사용
    //간단히 말해, "화면이 처음 나타났을 때 (또는 특정 값이 바뀌었을 때) 딱 한 번만 실행하고 싶은 코드가 있을 때" 사용합니다.
    //이 key 값이 변경될 때만 코드 블럭을 실행함. 지금 Unit(상수)이 사용되었고, 이 값은 절대 변하지 않기에 처음 딱 한번만 실행됨.
    //화면이 종료되면, 얘도 자동으로 끝남.

    // 샘플 데이터 (나중에 이제 백엔드한테 받을것임)--------
    val likedBooks = listOf(
        BookItem("책 제목", "작은이름 저자", "1234"),
        BookItem("책 제목", "작은이름 저자","1234"),
        BookItem("책 제목", "작은이름 저자","1234"),
        BookItem("책 제목", "작은이름 저자","1234")
    )

    val likedPlaylists = listOf(
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자")
    )

    /*val myBooks = listOf(
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자"),
        BookItem("책 제목", "작은이름 저자")
    )*/

    val recommendedPlaylists = listOf(
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자"),
        PlaylistItem("플리 제목", "저작권자")
    )
    //-----------


    //!화면!
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
            // 책이 없을 때 안내 메시지 표시
            if (myBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 추가한 책이 없습니다.\n'나의 책 추가' 버튼을 눌러 책을 추가해보세요!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                BookRow(
                    books = myBooks,
                    darkGreen = darkGreen,
                    onBookClick = {book->
                        // 책 클릭 시 다운로드 및 화면 전환
                        isDownloading = true
                        coroutineScope.launch {
                            try {
                                val bookFile = downloadBookFromServer(
                                    context = context,
                                    isbn = book.isbn
                                )

                                if (bookFile != null) {
                                    // EbookViewer로 이동 : Navigation
                                    onNavigateToEbookViewer(
                                        book.title,
                                        book.author,
                                        book.isbn,
                                        bookFile.absolutePath
                                    )
                                    /*openEbookViewer(
                                        context = context,
                                        bookTitle = book.title,
                                        bookAuthor = book.author,
                                        bookFilePath = bookFile.absolutePath
                                    )*/
                                } else {
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
                                isDownloading = false
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 제작한 플레이리스트
            SectionTitle("내가 제작한 플레이리스트")
            Spacer(modifier = Modifier.height(12.dp))
            PlaylistRow(recommendedPlaylists, darkGreen)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 다운로드 중 로딩 표시
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
                    CircularProgressIndicator(color = darkGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("책을 불러오는 중...", fontSize = 16.sp)
                }
            }
        }
    }

    // 책 추가 다이얼로그!!!!!
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { bookTitle, author, isbn, plot, fileUri ->
                // 책 추가 로직 구현    아마 이 아래에 백엔드로 보내는 함수?
                isUploading = true
                coroutineScope.launch {
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
                            val newBook = BookItem(
                                title = bookTitle,
                                author = author,
                                isbn = isbn
                            )
                            myBooks = myBooks + newBook  // 기존 목록에 새 책 추가

                            Toast.makeText(
                                context,
                                "책이 성공적으로 추가되었습니다",
                                Toast.LENGTH_SHORT
                            ).show()
                            showAddBookDialog = false
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
            darkGreen = darkGreen,
            isUploading = isUploading
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
                    maxLines = 10,
                    enabled = !isUploading

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
                        modifier = Modifier.padding(top = 8.dp)
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
        //확인 버튼.
        confirmButton = {
            Button(
                onClick = {
                    if (bookTitle.isNotBlank() && author.isNotBlank()
                        &&isbn.isNotBlank() && plot.isNotBlank()
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

// 데이터 클래스
data class BookItem(
    val title: String,
    val author: String,
    val isbn : String
)

data class PlaylistItem(
    val title: String,
    val creator: String
)
//data class

// 4. 파일 업로드 함수
suspend fun uploadBookToServer(
    context: Context,
    title: String,
    author: String,
    isbn: String,
    plot: String,
    fileUri: Uri
): BookUploadResponse {
    return withContext(Dispatchers.IO) {
        try {
            // URI에서 파일 읽기
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: throw IOException("파일을 열 수 없습니다")

            val fileName = getFileName(context, fileUri) ?: "book.epub"
            val fileBytes = inputStream.readBytes()
            inputStream.close()

            // RequestBody 생성
            val requestFile = fileBytes.toRequestBody(
                "application/epub+zip".toMediaTypeOrNull()
            )

            val filePart = MultipartBody.Part.createFormData(
                "file",
                fileName,
                requestFile
            )

            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val authorBody = author.toRequestBody("text/plain".toMediaTypeOrNull())
            val isbnBody = isbn.toRequestBody("text/plain".toMediaTypeOrNull())
            val plotBody = plot.toRequestBody("text/plain".toMediaTypeOrNull())

            // API 호출!!!!!!!!!!!!!!
            val response = Api.retrofitService.uploadBook(
                title = titleBody,
                author = authorBody,
                isbn = isbnBody,
                plot = plotBody,
                file = filePart
            )

            if (response.isSuccessful) {
                response.body() ?: BookUploadResponse(false, "응답이 비어있습니다")
            } else {
                BookUploadResponse(false, "서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            BookUploadResponse(false, "업로드 실패: ${e.message}")
        }
    }
}

// 5. 파일명 추출 헬퍼 함수
fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName
}


// 3. 책 다운로드 함수
suspend fun downloadBookFromServer(
    context: Context,
    isbn: String
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val response = Api.retrofitService.downloadBook(isbn)

            if (response.isSuccessful && response.body() != null) {
                // 앱 내부 저장소에 파일 저장
                val booksDir = File(context.filesDir, "books")
                if (!booksDir.exists()) {
                    booksDir.mkdirs()
                }

                val bookFile = File(booksDir, "$isbn.epub")

                response.body()?.let { responseBody ->
                    FileOutputStream(bookFile).use { output ->
                        responseBody.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                }

                bookFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// 4. EbookViewer 화면 전환 함수 (UserProfileActivity.kt에 추가)
/*fun openEbookViewer(
    context: Context,
    bookTitle: String,
    bookAuthor: String,
    bookFilePath: String
) {
    val intent = Intent(context, EbookViewerActivity::class.java).apply {
        putExtra("BOOK_TITLE", bookTitle)
        putExtra("BOOK_AUTHOR", bookAuthor)
        putExtra("BOOK_FILE_PATH", bookFilePath)
    }
    context.startActivity(intent)
}*/

@Preview
@Composable
fun UserProfilePreview() {
    UserProfileScreen()
}

