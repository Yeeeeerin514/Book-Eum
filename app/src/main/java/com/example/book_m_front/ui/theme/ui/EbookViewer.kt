package com.example.book_m_front.ui.theme.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import java.io.File


/*class EbookViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //선택한 책을 띄우기 위해
        // Intent에서 책 정보 받기
        val bookTitle = intent.getStringExtra("BOOK_TITLE") ?: "책 제목"
        val bookAuthor = intent.getStringExtra("BOOK_AUTHOR") ?: "저자"
        val bookFilePath = intent.getStringExtra("BOOK_FILE_PATH") ?: ""

        setContent {
            MaterialTheme {
                EbookViewerScreen(
                    bookTitle = bookTitle,
                    bookAuthor = bookAuthor,
                    bookFilePath = bookFilePath,
                    onBackClick = { finish() }
                )
            }
        }
    }
}*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerScreen(
    bookTitle: String,
    bookAuthor: String,
    bookIsbn : String,
    bookFilePath: String,
    onBackClick: () -> Unit,
    //곡 연결을 이전에 해서 받을까, 아님 여기서 곡 연결을 할까?(백엔드에 곡 요청) 여기서 해야되는거아님??계속 업데이트 되니까
    //근데 그럼 isbn도 받아야겠네,,->받앗다.
) {   //책 정보 건네받기
    var showMusicPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var bookContent by remember { mutableStateOf("책 내용을 불러오는 중...") }
    val darkGreen = AppColors.DeepGreen


    // 책 내용 로드
    LaunchedEffect(bookFilePath) {
        if (bookFilePath.isNotEmpty()) {
            // TODO: EPUB 파일 파싱 (nl.siegmann.epublib 라이브러리 사용 권장)
            // 임시로 파일 존재 여부만 확인
            val file = File(bookFilePath)
            bookContent = if (file.exists()) {
                "EPUB 파일이 로드되었습니다.\n파일 경로: $bookFilePath\n\n" +
                        "실제 EPUB 파싱을 위해서는 EPUB 라이브러리를 추가해야 합니다."
            } else {
                "파일을 찾을 수 없습니다."
            }
        }
    }

    //음악플리 백에 요청하고 가져오기
    //플리 중 첫번째 곡 재생하기

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 상단 헤더
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = bookTitle,
                            color = AppColors.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = bookAuthor,
                            color = AppColors.Black.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.Black,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 메뉴 */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = AppColors.Black,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.White,
                )
            )

            // 이북 콘텐츠 영역 (터치 감지)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.White)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showMusicPlayer = !showMusicPlayer
                            }
                        )
                    }
            ) {
                EbookContent(bookContent)
            }
        }

        // 슬라이드 업 음악 플레이어
        AnimatedVisibility(
            visible = showMusicPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MusicPlayerPanel(
                isPlaying = isPlaying,
                onPlayPauseClick = { isPlaying = !isPlaying },
                backgroundColor = AppColors.DeepGreen
            )
        }
    }
}

@Composable
fun EbookContent(content: String) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = content,
            fontSize = 16.sp,
            lineHeight = 28.sp,
            color = AppColors.Black,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun MusicPlayerPanel(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // "Now Playing 🎵" 텍스트
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Now Playing 🎵",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }

            Divider(
                color = Color.White.copy(alpha = 1.0f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 앨범 아트 (정사각형 박스)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 곡 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "음악 이름",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "아티스트 | 앨범명",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 재생 버튼
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}



@Preview
@Composable
fun EbookViewerScreenPreview() {
    var bookContent by remember { mutableStateOf("책 내용을 불러오는 중...") }

    //EbookViewerScreen(bookContent, "as", "as", onBackClick = {})
    MusicPlayerPanel(
        isPlaying = true,
        onPlayPauseClick = { },
        backgroundColor = AppColors.DeepGreen
    )

}