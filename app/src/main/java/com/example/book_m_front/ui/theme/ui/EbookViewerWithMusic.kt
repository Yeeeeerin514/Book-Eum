package com.example.book_m_front.ui.theme.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.book_m_front.network.Api
import com.example.book_m_front.network.downloadAndGetBookPath
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel
import com.example.book_m_front.ui.theme.viewmodel.formatTime
import com.example.book_m_front.util.EpubContent
import com.example.book_m_front.util.SimpleEpubParser
import kotlinx.coroutines.launch
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerWithMusicScreen(
    bookTitle: String,
    bookAuthor: String,
    bookIsbn: String,
    testFilePath: String? = null,
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

    // ✅ 수정: ViewModel에서 제공하는 상태들만 사용
    val playerState by musicPlayerViewModel.playerState.collectAsState()
    val playlist by musicPlayerViewModel.playlist.collectAsState()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsState()
    val currentPosition by musicPlayerViewModel.currentPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()
    val currentTrack by musicPlayerViewModel.currentTrack.collectAsState()

    // ✅ 수정: 진행률 계산 (0.0 ~ 1.0)
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // EPUB 파일 다운로드 및 파싱
    LaunchedEffect(bookIsbn) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 테스트 파일이 있으면 테스트 파일 사용
                if (testFilePath != null) {
                    epubContent = SimpleEpubParser.parseEpub(context, testFilePath)
                } else {
                    // 서버에서 EPUB 파일 다운로드
                    val localBookPath = downloadAndGetBookPath(context, bookIsbn)

                    if (localBookPath != null) {
                        epubContent = SimpleEpubParser.parseEpub(context, localBookPath)
                    } else {
                        errorMessage = "책 파일을 불러오는 데 실패했습니다."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "오류가 발생했습니다: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 음악 플레이리스트 로드
    LaunchedEffect(bookIsbn) {
        scope.launch {
            try {
                val response = Api.retrofitService.getPlaylist(bookIsbn)
                if (response.isSuccessful && response.body() != null) {
                    val playlistData = response.body()!!.playlist
                    if (playlistData.isNotEmpty()) {
                        musicPlayerViewModel.setPlaylist(playlistData)
                        // 첫 번째 곡 자동 재생 (선택적)
                        // musicPlayerViewModel.playPlaylist(playlistData, 0)
                    }
                } else {
                    println("플레이리스트 정보 로드 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                println("플레이리스트 정보 로드 중 오류 발생: ${e.message}")
            }
        }
    }

    // ✅ 수정: 챕터 변경 시 음악 변경 (안전하게 처리)
    LaunchedEffect(currentChapterIndex) {
        if (playlist.isNotEmpty()) {
            val trackIndex = currentChapterIndex % playlist.size
            musicPlayerViewModel.playTrack(playlist[trackIndex])
        }
    }

    // UI
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFBF5)
    val textColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF2C2C2C)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)) {
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
                        Icon(Icons.Default.TextFields, "글자 크기", tint = textColor)
                    }
                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                        Icon(
                            if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            "다크모드 전환",
                            tint = textColor
                        )
                    }
                    IconButton(onClick = { showMusicPlayer = !showMusicPlayer }) {
                        Icon(
                            if (showMusicPlayer) Icons.Default.MusicOff else Icons.Default.MusicNote,
                            "음악 플레이어",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )

            // 본문 영역
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AppColors.DeepGreen
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(errorMessage!!, color = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onBackClick) {
                                Text("돌아가기")
                            }
                        }
                    }
                    epubContent != null -> {
                        ImprovedEbookContent(
                            chapter = epubContent!!.chapters.getOrNull(currentChapterIndex),
                            fontSize = fontSize,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            onPreviousChapter = {
                                if (currentChapterIndex > 0) {
                                    currentChapterIndex--
                                }
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
            if (epubContent != null && epubContent!!.chapters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (currentChapterIndex > 0) currentChapterIndex--
                        },
                        enabled = currentChapterIndex > 0
                    ) {
                        Text("이전")
                    }
                    Text(
                        "${currentChapterIndex + 1} / ${epubContent!!.chapters.size}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        color = textColor
                    )
                    Button(
                        onClick = {
                            if (currentChapterIndex < epubContent!!.chapters.size - 1) {
                                currentChapterIndex++
                            }
                        },
                        enabled = currentChapterIndex < epubContent!!.chapters.size - 1
                    ) {
                        Text("다음")
                    }
                }
            }
        }

        // ✅ 음악 플레이어 (수정됨)
        AnimatedVisibility(
            visible = showMusicPlayer && playlist.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            MusicPlayerCard(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                progress = progress,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPause = { musicPlayerViewModel.togglePlayPause() },
                onSeek = { position ->
                    musicPlayerViewModel.seekTo((position * duration).toLong())
                },
                onNext = { musicPlayerViewModel.skipToNext() },
                onPrevious = { musicPlayerViewModel.skipToPrevious() },
                onClose = { showMusicPlayer = false }
            )
        }

        // 챕터 목록 다이얼로그
        if (showChapterList && epubContent != null) {
            ChapterListDialog(
                chapters = epubContent!!.chapters,
                currentIndex = currentChapterIndex,
                onChapterSelect = { index ->
                    currentChapterIndex = index
                    showChapterList = false
                },
                onDismiss = { showChapterList = false }
            )
        }

        // 글자 크기 조절 다이얼로그
        if (showFontSizeDialog) {
            FontSizeDialog(
                currentSize = fontSize,
                onSizeChange = { fontSize = it },
                onDismiss = { showFontSizeDialog = false }
            )
        }
    }
}

// ✅ 수정: 음악 플레이어 카드
@Composable
fun MusicPlayerCard(
    currentTrack: Music?,
    isPlaying: Boolean,
    progress: Float,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.DeepGreen
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 닫기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        "닫기",
                        tint = Color.White
                    )
                }
            }

            // 곡 정보
            Text(
                text = currentTrack?.title ?: "재생 중인 곡 없음",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = currentTrack?.artist ?: "",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 진행 바
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
                    formatTime(currentPosition / 1000),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(
                    formatTime(duration / 1000),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 컨트롤 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        "이전 곡",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "재생/일시정지",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Default.SkipNext,
                        "다음 곡",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImprovedEbookContent(
    chapter: com.example.book_m_front.util.Chapter?,
    fontSize: Int,
    textColor: Color,
    backgroundColor: Color,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    if (chapter == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("챕터 내용을 불러올 수 없습니다.", color = textColor)
        }
        return
    }

    val bodyStyle = """
        <style>
            body {
                color: #${Integer.toHexString(textColor.toArgb()).substring(2)};
                background-color: #${Integer.toHexString(backgroundColor.toArgb()).substring(2)};
                font-size: ${fontSize}px;
                line-height: 1.6;
                padding: 16px;
            }
            img {
                max-width: 100%;
                height: auto;
            }
        </style>
    """.trimIndent()

    val styledHtmlContent = bodyStyle + chapter.content
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            setBackgroundColor(0)
        }
    }

    LaunchedEffect(styledHtmlContent) {
        webView.loadDataWithBaseURL(
            null,
            styledHtmlContent,
            "text/html",
            "utf-8",
            null
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webView.stopLoading()
            } catch (_: Exception) {}
            webView.destroy()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                val width = size.width
                if (offset.x < width / 4) {
                    onPreviousChapter()
                } else if (offset.x > width * 3 / 4) {
                    onNextChapter()
                }
            }
        }
    ) {
        AndroidView(
            factory = { webView },
            update = { },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ChapterListDialog(
    chapters: List<com.example.book_m_front.util.Chapter>,
    currentIndex: Int,
    onChapterSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("챕터 목록", fontWeight = FontWeight.Bold) },
        text = {
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex)
            LazyColumn(state = listState) {
                itemsIndexed(chapters) { index, chapter ->
                    Text(
                        text = "${index + 1}. ${chapter.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterSelect(index) }
                            .padding(vertical = 12.dp),
                        color = if (index == currentIndex) MaterialTheme.colorScheme.primary else Color.Unspecified,
                        fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

@Composable
fun FontSizeDialog(
    currentSize: Int,
    onSizeChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("글자 크기 조절") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "가나다라",
                    fontSize = currentSize.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("작게", fontSize = 12.sp)
                    Slider(
                        value = currentSize.toFloat(),
                        onValueChange = { onSizeChange(it.toInt()) },
                        valueRange = 12f..28f,
                        steps = 15,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("크게", fontSize = 18.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}