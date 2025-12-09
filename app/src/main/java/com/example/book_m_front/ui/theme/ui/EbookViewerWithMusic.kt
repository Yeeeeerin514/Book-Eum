package com.example.book_m_front.ui.theme.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel
import com.example.book_m_front.ui.theme.viewmodel.formatTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerWithMusicScreen(
    bookTitle: String,
    bookAuthor: String,
    bookIsbn: String,
    bookFilePath: String,
    onBackClick: () -> Unit,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // EPUB 관련 상태
    var epubContent by remember { mutableStateOf<EpubContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentChapterIndex by remember { mutableStateOf(0) }

    // UI 설정
    var showChapterList by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(16) }
    var isDarkMode by remember { mutableStateOf(false) }

    // 음악 플레이어 상태
    var showMusicPlayer by remember { mutableStateOf(false) }
    val playerState by musicPlayerViewModel.playerState.collectAsState()
    val playlist by musicPlayerViewModel.playlist.collectAsState()
    val currentTrackIndex by musicPlayerViewModel.currentTrackIndex.collectAsState()
    val progress by musicPlayerViewModel.progress.collectAsState()
    val currentPosition by musicPlayerViewModel.currentPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()

    val currentTrack = playlist.getOrNull(currentTrackIndex)

    // ExoPlayer 초기화
    LaunchedEffect(Unit) {
        musicPlayerViewModel.initializePlayer(context)
    }

    // EPUB 파일 로드
    LaunchedEffect(bookFilePath) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                epubContent = when {
                    bookFilePath.endsWith(".epub", ignoreCase = true) -> {
                        SimpleEpubParser.parseEpub(context, bookFilePath)
                    }
                    bookFilePath.endsWith(".txt", ignoreCase = true) -> {
                        SimpleTxtParser.parseTxt(context, bookFilePath)
                    }
                    else -> {
                        SimpleEpubParser.parseEpub(context, bookFilePath)
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // 음악 플레이리스트 로드
    LaunchedEffect(bookIsbn) {
        scope.launch {
            try {
                val fetchedPlaylist = fetchPlaylistFromBackend(bookIsbn)
                if (fetchedPlaylist.isNotEmpty()) {
                    musicPlayerViewModel.setPlaylist(fetchedPlaylist)
                }
            } catch (e: Exception) {
                // 음악 로드 실패 시 에러 처리
            }
        }
    }

    // 챕터 변경 시 음악 변경 (선택적)
    LaunchedEffect(currentChapterIndex) {
        // TODO: 챕터별로 다른 음악을 재생하려면 여기에 로직 추가
        // 예: musicPlayerViewModel.playTrackAt(currentChapterIndex % playlist.size)
    }

    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFBF5)
    val textColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF2C2C2C)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = epubContent?.title ?: bookTitle,
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        if (epubContent != null && epubContent!!.chapters.isNotEmpty()) {
                            Text(
                                text = "${currentChapterIndex + 1} / ${epubContent!!.chapters.size}",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(Icons.Default.List, "챕터 목록", tint = textColor)
                    }
                    IconButton(onClick = { showFontSizeDialog = true }) {
                        Icon(Icons.Default.Settings, "설정", tint = textColor)
                    }
                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                        Icon(
                            if (isDarkMode) Icons.Default.Star else Icons.Default.Face,
                            "테마 변경",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )

            // 콘텐츠 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (playlist.isNotEmpty()) {
                                    showMusicPlayer = !showMusicPlayer
                                }
                            }
                        )
                    }
            ) {
                when {
                    isLoading -> {
                        LoadingScreen()
                    }
                    errorMessage != null -> {
                        ErrorScreen(errorMessage!!)
                    }
                    epubContent != null -> {
                        ImprovedEbookContent(
                            chapter = epubContent!!.chapters.getOrNull(currentChapterIndex),
                            fontSize = fontSize,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            onPreviousChapter = {
                                if (currentChapterIndex > 0) currentChapterIndex--
                            },
                            onNextChapter = {
                                if (currentChapterIndex < epubContent!!.chapters.size - 1) {
                                    currentChapterIndex++
                                }
                            }
                        )
                    }
                }
            }

            // 하단 네비게이션
            if (epubContent != null && epubContent!!.chapters.size > 1) {
                ChapterNavigation(
                    currentChapterIndex = currentChapterIndex,
                    totalChapters = epubContent!!.chapters.size,
                    currentChapterTitle = epubContent!!.chapters.getOrNull(currentChapterIndex)?.title ?: "",
                    backgroundColor = backgroundColor,
                    textColor = textColor,
                    onPreviousClick = { if (currentChapterIndex > 0) currentChapterIndex-- },
                    onNextClick = {
                        if (currentChapterIndex < epubContent!!.chapters.size - 1) currentChapterIndex++
                    }
                )
            }
        }

        // 음악 플레이어
        AnimatedVisibility(
            visible = showMusicPlayer && playlist.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            EnhancedMusicPlayerPanel(
                track = currentTrack,
                isPlaying = playerState.isPlaying,
                progress = progress,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPauseClick = { musicPlayerViewModel.togglePlayPause() },
                onPreviousClick = { musicPlayerViewModel.playPrevious() },
                onNextClick = { musicPlayerViewModel.playNext() },
                onSeek = { musicPlayerViewModel.seekToProgress(it) },
                backgroundColor = AppColors.DeepGreen
            )
        }

        // 다이얼로그들
        if (showChapterList && epubContent != null) {
            ChapterListDialog(
                chapters = epubContent!!.chapters,
                currentIndex = currentChapterIndex,
                onChapterSelect = {
                    currentChapterIndex = it
                    showChapterList = false
                },
                onDismiss = { showChapterList = false }
            )
        }

        if (showFontSizeDialog) {
            FontSizeDialog(
                currentSize = fontSize,
                onSizeChange = { fontSize = it },
                onDismiss = { showFontSizeDialog = false }
            )
        }
    }
}

@Composable
fun EnhancedMusicPlayerPanel(
    track: Music?,
    isPlaying: Boolean,
    progress: Float,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Float) -> Unit,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Now Playing 🎵",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "닫기",
                    tint = Color.White
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (track != null) {
                // 앨범 아트
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 곡 정보
                Text(
                    text = track.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${track.artist} | ${track.album}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 재생 진행바
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )

                // 시간 표시
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatTime(duration),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 컨트롤 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            "이전",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = onPlayPauseClick,
                        containerColor = Color.White,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                            if (isPlaying) "일시정지" else "재생",
                            tint = backgroundColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = onNextClick) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            "다음",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "음악을 불러오는 중...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ChapterNavigation(
    currentChapterIndex: Int,
    totalChapters: Int,
    currentChapterTitle: String,
    backgroundColor: Color,
    textColor: Color,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPreviousClick,
                enabled = currentChapterIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.DeepGreen)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, "이전")
                Text("이전")
            }

            Text(
                text = currentChapterTitle,
                color = textColor,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

            Button(
                onClick = onNextClick,
                enabled = currentChapterIndex < totalChapters - 1,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.DeepGreen)
            ) {
                Text("다음")
                Icon(Icons.Default.KeyboardArrowRight, "다음")
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.DeepGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("책을 불러오는 중...")
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("오류 발생", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, textAlign = TextAlign.Center)
        }
    }
}