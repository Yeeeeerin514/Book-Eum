package com.example.book_m_front.ui.theme.ui

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.book_m_front.network.dto.MusicTrack
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel
import com.example.book_m_front.ui.theme.viewmodel.formatTime
import com.example.book_m_front.util.EpubContent
import com.example.book_m_front.util.SimpleEpubParser
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.book_m_front.ui.theme.musicplayer.MusicPlayerUI
import com.example.book_m_front.ui.theme.viewmodel.EbookViewModel
import com.example.book_m_front.util.Chapter
import com.example.book_m_front.util.SafeEpubParser
import kotlinx.coroutines.flow.MutableStateFlow

private const val TAG = "EbookViewerWithMusic"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerWithMusicScreen(
    bookTitle: String,
    bookAuthor: String,
    bookIsbn: String,
    testFilePath: String? = null,
    onBackClick: () -> Unit,
    musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel(),
    ebookViewModel: EbookViewModel = viewModel()
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
    var showMusicPlayer by remember { mutableStateOf(true) }

    // ✅ ViewModel에서 제공하는 상태들
    val playerState by musicPlayerViewModel.playerState.collectAsState()
    val playlist by musicPlayerViewModel.playlist.collectAsState()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsState()
    val currentPosition by musicPlayerViewModel.currentPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()
    val currentTrack by musicPlayerViewModel.currentTrack.collectAsState()

    // ✅ 다운로드 관련 상태
    val isDownloading by musicPlayerViewModel.isDownloading.collectAsState()
    val downloadProgress by musicPlayerViewModel.downloadProgress.collectAsState()
    val firstChapterReady by musicPlayerViewModel.firstChapterReady.collectAsState()
    val localPlaylistPaths by musicPlayerViewModel.localPlaylistPaths.collectAsState()

    // ✅ 진행률 계산 (0.0 ~ 1.0)
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
                    epubContent = SafeEpubParser.parseEpub(context, testFilePath)
                } else {
                    // 서버에서 EPUB 파일 다운로드
                    Log.d(TAG,"서버에서 EPUB 파일 다운로드해서 로컬 캐시에 저장하고, 그 저장 경로 받아옴.")
                    val localBookPath = downloadAndGetBookPath(context, bookIsbn)

                    if (localBookPath != null) {
                        Log.d(TAG,"epub파일이 저장된 로컬 경로로부터, 파일을 읽어와서 파싱해옴.")
                        epubContent = SafeEpubParser.parseEpub(context, localBookPath)
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

    // ✅ ISBN으로 자동 다운로드 및 재생
    LaunchedEffect(bookIsbn) {
        Log.d(TAG, "🎵 음악 플레이리스트 다운로드 시작: ISBN=$bookIsbn")
        musicPlayerViewModel.loadAndPlayPlaylist(bookIsbn)
    }

    // ✅ 챕터 변경 시 음악 변경 (로컬 파일 재생)
    LaunchedEffect(currentChapterIndex, localPlaylistPaths) {
        if (localPlaylistPaths.isNotEmpty()) {
            val trackIndex = currentChapterIndex % localPlaylistPaths.size
            Log.d(TAG, "📖 챕터 변경: $currentChapterIndex → 음악 인덱스: $trackIndex")
            musicPlayerViewModel.playLocalFile(localPlaylistPaths[trackIndex])
        }
    }

    // UI
    val backgroundColor = if (isDarkMode) Color(0xFF222522) else Color(0xFFF5F8F5)
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
                            if (showMusicPlayer) Icons.Default.MusicNote else Icons.Default.MusicOff,
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF2D5F4D)
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
                    /*epubContent != null -> {
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
                    }*/
                }
            }

            // 하단 네비게이션 버튼
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

            // ✅ 음악 플레이어 (MusicPlayerUI 사용)
            AnimatedVisibility(
                visible = showMusicPlayer,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                //modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                // MusicPlayerUI를 Card로 감싸서 하단에 표시
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f), // 화면의 60% 높이
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // MusicPlayerUI 사용
                        MusicPlayerUI(viewModel = musicPlayerViewModel)

                        // 닫기 버튼
                        IconButton(
                            onClick = { showMusicPlayer = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
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

// ============================================
// ImprovedEbookContent - WebView 기반 이북 뷰어
// ============================================

@Composable
fun ImprovedEbookContent(
    chapter: Chapter?,
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

// ============================================
// 챕터 목록 다이얼로그
// ============================================

@Composable
fun ChapterListDialog(
    chapters: List<Chapter>,
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

// ============================================
// 글자 크기 다이얼로그
// ============================================

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
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
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

// ============================================
// Mock 데이터 (프리뷰용)
// ============================================

private val mockEpubContent = EpubContent(
    title = "어린왕자",
    author = "생텍쥐페리",
    chapters = listOf(
        Chapter("1장", "<p>어렸을 때 나는 원시림에 관한 책에서 멋진 그림을 본 적이 있다...</p>"),
        Chapter("2장", "<p>나는 사하라 사막에서 살았다...</p>"),
        Chapter("3장", "<p>어린 왕자가 어디서 왔는지 알기까지는 시간이 걸렸다...</p>")
    )
)

private val mockPlaylist = listOf(
    Music("1", "별", "윤하", ""),
    Music("2", "사계", "태연", ""),
    Music("3", "밤편지", "아이유", "")
)
// ============================================
// 1. 정상 로드 상태 (기본)
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "1. 이북 뷰어 - 라이트 모드",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EbookViewerPreview_Light() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F8F5))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("어린왕자", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("1 / 3", fontSize = 12.sp, color = Color.Gray)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ArrowBack, "뒤로가기")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.List, null) }
                        IconButton(onClick = {}) { Icon(Icons.Default.TextFields, null) }
                        IconButton(onClick = {}) { Icon(Icons.Default.DarkMode, null) }
                        IconButton(onClick = {}) { Icon(Icons.Default.MusicNote, null) }
                    }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Text("어른들은 숫자를 좋아한다...", fontSize = 16.sp, lineHeight = 28.sp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {}, enabled = false) { Text("이전") }
                    Text("1 / 3", modifier = Modifier.align(Alignment.CenterVertically))
                    Button(onClick = {}) { Text("다음") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "2. 음악 플레이어 열림",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun EbookViewerPreview_WithMusicPlayer() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F8F5))) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("어린왕자") },
                    navigationIcon = {
                        IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, null) }
                    }
                )
                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    Text("본문 내용...")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("플레이리스트", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    mockPlaylist.forEach { music ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF2D5F4D), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, null, tint = Color.White)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(music.title, fontWeight = FontWeight.Bold)
                                Text(music.artist, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "3. 챕터 목록", showBackground = true)
@Composable
fun ChapterListPreview() {
    MaterialTheme {
        ChapterListDialog(
            chapters = mockEpubContent.chapters,
            currentIndex = 1,
            onChapterSelect = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "4. 글자 크기", showBackground = true)
@Composable
fun FontSizePreview() {
    MaterialTheme {
        FontSizeDialog(
            currentSize = 16,
            onSizeChange = {},
            onDismiss = {}
        )
    }
}
