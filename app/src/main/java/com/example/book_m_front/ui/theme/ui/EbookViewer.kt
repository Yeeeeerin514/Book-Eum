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


class EbookViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EbookViewerScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerScreen() {   //책 정보 건네받기
    var showMusicPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    val darkGreen = Color(0xFF2D4A3E)

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
                    Text(
                        text = "책 제목",
                        color = AppColors.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* 뒤로 가기 */ }) {
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
                EbookContent()
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
fun EbookContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "책 내용 암암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암 어떠고저쩌고 가나다라마바사 내용 암암암암암",
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
    EbookViewerScreen()
    MusicPlayerPanel(
        isPlaying = true,
        onPlayPauseClick = { },
        backgroundColor = AppColors.DeepGreen
    )

}