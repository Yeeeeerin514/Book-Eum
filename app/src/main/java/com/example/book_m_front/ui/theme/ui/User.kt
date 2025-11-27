package com.example.book_m_front.ui.theme.ui

import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import com.example.book_m_front.R
import kotlinx.coroutines.launch
import com.example.book_m_front.network.dto.BookItem


// 7. 필요한 import 추가

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.book_m_front.network.ServerRequestAndResponse.getFileName
import com.example.book_m_front.network.ServerRequestAndResponse.uploadBookToServer
/*
import com.example.book_m_front.ui.theme.ui.book.BookItem
*/
import com.example.book_m_front.ui.theme.ui.book.BookRow
import com.example.book_m_front.ui.theme.ui.book.handleBookClickToEbookViewer
import com.example.book_m_front.ui.theme.ui.playlist.PlaylistItem
import com.example.book_m_front.ui.theme.ui.playlist.PlaylistRow


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


    var likedBooks by remember { mutableStateOf(listOf<BookItem>()) }
    var likedPlaylists by remember { mutableStateOf(listOf<PlaylistItem>()) }
    var recommendedPlaylists by remember { mutableStateOf(listOf<PlaylistItem>()) }

    // 앱 시작 시 서버에서 내가 추가한 책 목록 불러오기
    LaunchedEffect(Unit) {
        // TODO: 서버에서 사용자의 책 목록을 불러오는 API 호출
        //likedBooks..얘네에 저장
    }
    //LaunchedEffect : 네트워크 요청, 데이터베이스 조회, 애니메이션 실행과 같이 Composable 함수의 일반적인 실행 흐름과 다른 생명주기를 갖는 작업을 처리할 때 사용
    //간단히 말해, "화면이 처음 나타났을 때 (또는 특정 값이 바뀌었을 때) 딱 한 번만 실행하고 싶은 코드가 있을 때" 사용합니다.
    //이 key 값이 변경될 때만 코드 블럭을 실행함. 지금 Unit(상수)이 사용되었고, 이 값은 절대 변하지 않기에 처음 딱 한번만 실행됨.
    //화면이 종료되면, 얘도 자동으로 끝남.

    // 샘플 데이터
    /*val examplelikedBooks = listOf(
        BookItem("책 제목", "작은이름 저자", "1234"),
        BookItem("책 제목", "작은이름 저자","1234"),
        BookItem("책 제목", "작은이름 저자","1234"),
        BookItem("책 제목", "작은이름 저자","1234")
    )*/

    val examplelikedPlaylists = listOf(
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

    val examplerecommendedPlaylists = listOf(
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
            BookRow(
                books = likedBooks,
                darkGreen = darkGreen,
                onBookClick = { book ->
                    //함수로 호출하고 싶은데
                    handleBookClickToEbookViewer(
                        book = book,
                        context = context,
                        coroutineScope = coroutineScope,
                        onStartLoading = { isDownloading = true },
                        onFinishLoading = { isDownloading = false },
                        onNavigateToEbookViewer = onNavigateToEbookViewer
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 좋아요한 플레이리스트
            SectionTitle("내가 좋아요한 플레이리스트")
            Spacer(modifier = Modifier.height(12.dp))
            PlaylistRow(likedPlaylists, darkGreen)

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 제작한 플레이리스트
            SectionTitle("내가 제작한 플레이리스트")
            Spacer(modifier = Modifier.height(12.dp))
            PlaylistRow(recommendedPlaylists, darkGreen)

            Spacer(modifier = Modifier.height(32.dp))

            // 내가 추가한 책
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("내가 추가한 책")
                //나의 책 추가 버튼
                Button(
                    onClick = { showAddBookDialog = true }, //얘를 true로 만듦으로 -> AddBookDialog를 부르는 로직을 실행
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
        }
    }

    // 다운로드 중에 보여줄 로딩 화면
    if (isDownloading) {
        IsDownloadingScreen()
    }

    // showAddBookDialog가 true가 되면 AddBookDialog를 보여줌.
    if (showAddBookDialog) {
        //'확인'을 누르면 책을 추가하는 로직을 실행함.
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onConfirm = { bookTitle, author, isbn, plot, fileUri ->
                // 책 추가 로직 구현    아마 이 아래에 백엔드로 보내는 함수?
                isUploading = true
                coroutineScope.launch {
                    try {
                        //책을 서버에 업로드
                        val result = uploadBookToServer(
                            context = context,
                            title = bookTitle,
                            author = author,
                            isbn = isbn,
                            plot = plot,
                            fileUri = fileUri
                        )
                        //책을 서버에 올리는 것에 성공했다면
                        if (result.success) {   //result는 BookUploadResponse 데이터 클래스의 객체임.
                            //내가 추가한 책 목록은 없음!! 그냥 추가만 되고 끝.
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
fun IsDownloadingScreen(){
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

//TODO : 유저 정보 서버에서 받아오기
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

    //UI
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.DeepGreen
                    ),
                    onClick = {
                    // 파일 유형 필터 설정 : e-pub만 받도록 함.
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

                if (isUploading) {  //책 업로드 중에 보여줄 화면
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
                        onConfirm(bookTitle, author, isbn, plot, selectedFileUri!!) //<-위에 본 코드에서 정의한 함수임. (왜 걔만 위에잇는거지?)
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
    //IsDownloadingScreen()
    UserProfileScreen()
}

