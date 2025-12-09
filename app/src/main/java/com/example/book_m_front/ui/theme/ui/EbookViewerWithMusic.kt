package com.example.book_m_front.ui.theme.ui

import android.content.Context
//import android.webkit.WebSettings
//import android.webkit.WebView
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.book_m_front.ui.theme.viewmodel.EbookViewModel
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel
import com.example.book_m_front.ui.theme.viewmodel.formatTime
import com.example.book_m_front.util.EpubContent
import com.example.book_m_front.util.SimpleEpubParser
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerWithMusicScreen(
    bookTitle: String,
    bookAuthor: String,
    bookIsbn: String,
    //bookFilePath: String,
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
    //받은 isbn으로 bookFilePath를 요청해서 받아옴. 아니 애초에 파일 자체를 받아옴.
    // EPUB 파일 다운로드 및 파싱
    LaunchedEffect(bookIsbn) { // bookIsbn이 변경될 때마다 이 블록이 실행됩니다.
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. 서버에서 EPUB 파일을 다운로드하고 로컬 파일 경로를 받아옵니다.
                val localBookPath = downloadAndGetBookPath(context, bookIsbn)

                if (localBookPath != null) {
                    // 2. 성공적으로 다운로드한 경우, 해당 경로의 파일을 파싱합니다.
                    // 이전에 존재하던 SimpleEpubParser를 여기에 구현해야 합니다.
                    epubContent = SimpleEpubParser.parseEpub(context, localBookPath)
                } else {
                    // 다운로드 실패 시 에러 메시지를 설정합니다.
                    errorMessage = "책 파일을 불러오는 데 실패했습니다."
                }
            } catch (e: Exception) {
                // 네트워크 오류 또는 파싱 중 발생한 모든 예외를 처리합니다.
                errorMessage = "오류가 발생했습니다: ${e.message}"
                e.printStackTrace() // 디버깅을 위해 에러 로그를 출력합니다.
            } finally {
                // 로딩 상태를 종료합니다.
                isLoading = false
            }
        }
    }

    /*// EPUB 파일 로드
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
*/
    // 음악 플레이리스트 로드
    LaunchedEffect(bookIsbn) {
        scope.launch {
            try {
                val fetchedPlaylist = Api.retrofitService.getPlaylist(bookIsbn)
                if (fetchedPlaylist != null) {
                    val playList = fetchedPlaylist.body()
                    if(playList?.playlist != null){
                        musicPlayerViewModel.setPlaylist(playList.playlist)
                    }
                } else {
                    println("플레이리스트 정보 로드 실패: ${fetchedPlaylist.code()}")
                }
            } catch (e: Exception) {
                println("플레이리스트 정보 로드 중 오류 발생: ${e.message}")            }
        }
    }

    // 챕터 변경 시 음악 변경 (선택적)
    LaunchedEffect(currentChapterIndex) {
        // TODO: 챕터별로 다른 음악을 재생하려면 여기에 로직 추가
        musicPlayerViewModel.playTrackAt(currentChapterIndex % playlist.size)
    }


    //UI~~~

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



@Composable
fun ImprovedEbookContent(
    chapter: com.example.book_m_front.util.Chapter?, // Chapter 모델의 경로를 정확히 지정
    fontSize: Int,
    textColor: Color,
    backgroundColor: Color,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    if (chapter == null) {
        // 챕터 내용이 없는 경우 (로딩 중이거나 오류)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("챕터 내용을 불러올 수 없습니다.", color = textColor)
        }
        return
    }

    // WebView에 적용할 CSS 스타일 (글자 크기, 색상 등)
    // chapter.content (HTML)의 <body> 안에 이 스타일을 적용합니다.
    val bodyStyle = """
        <style>
            body {
                color: #${Integer.toHexString(textColor.toArgb()).substring(2)};
                background-color: #${Integer.toHexString(backgroundColor.toArgb()).substring(2)};
                font-size: ${fontSize}px;
                line-height: 1.6;
                padding: 16px;
            }
            /* 이미지나 다른 요소들이 화면을 넘어가지 않도록 설정 */
            img {
                max-width: 100%;
                height: auto;
            }
        </style>
    """.trimIndent()

    // 기존 HTML 본문에 스타일을 추가
    val styledHtmlContent = bodyStyle + chapter.content


    // WebView의 상태를 기억하기 위한 state
    //val webViewState = rememberWebViewState(data = styledHtmlContent, baseUrl = null)

    val context = LocalContext.current

// 1) WebView 인스턴스를 Compose 재구성(recomposition) 동안 유지
    val webView = remember {
        WebView(context).apply {
            // 안전한 기본 설정
            settings.apply {
                javaScriptEnabled = true // 필요하면 활성화 (주의: 보안)
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            // WebView가 외부 브라우저를 열지 않게 막음
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // 필요시 로드 완료 후 작업
                    super.onPageFinished(view, url)
                }
            }
            // 배경을 투명하게 만들어 Compose 배경색이 보이도록 함
            setBackgroundColor(0)
        }
    }

    // 2) styledHtmlContent가 바뀔 때마다 웹뷰에 로드
    LaunchedEffect(styledHtmlContent) {
        // 상대 경로(이미지 등)를 제대로 로드하려면 baseUrl을 적절히 설정해야 함.
        // 만약 EPUB 내 이미지를 앱 내부에 추출해 로컬 폴더(예: file:///data/data/<pkg>/files/epub/<id>/)
        // 로 저장해두었다면 해당 경로를 baseUrl로 넣어주면 이미지가 잘 나옴.
        //
        // 지금은 간단히 baseUrl=null로 로드 (상대 리소스 필요 없을 때 사용)
        webView.loadDataWithBaseURL(
            /* baseUrl = */ null,
            /* data = */ styledHtmlContent,
            /* mimeType = */ "text/html",
            /* encoding = */ "utf-8",
            /* historyUrl = */ null
        )
    }

    // 3) 컴포저블이 소멸될 때 WebView 정리 — 메모리 누수 방지
    DisposableEffect(Unit) {
        onDispose {
            try {
                webView.stopLoading()
            } catch (_: Exception) {}
            //webView.webViewClient = null
            webView.destroy()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
        // 챕터 이동을 위한 좌우 탭 감지
        .pointerInput(Unit) {
            detectTapGestures { offset ->
                val width = size.width
                if (offset.x < width / 4) { // 화면의 왼쪽 1/4을 탭하면 이전 챕터
                    onPreviousChapter()
                } else if (offset.x > width * 3 / 4) { // 화면의 오른쪽 1/4을 탭하면 다음 챕터
                    onNextChapter()
                }
            }
        }
    ) {
        AndroidView(
            factory = { webView },
            update = { vw ->
                // 필요하면 추가 업데이트 처리
                // 예: 접근성 설정, JavaScript bridge 연결 등
            },
            modifier = Modifier.fillMaxSize()
        )
/*        // WebView를 사용하여 HTML 콘텐츠 표시
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize(),
            onCreated = { webView ->
                // WebView의 배경을 투명하게 만들어 Compose 배경색이 보이도록 함
                webView.setBackgroundColor(0)
            }
        )*/
    }
}


@Composable
fun ChapterListDialog(
    chapters: List<com.example.book_m_front.util.Chapter>, // Chapter 모델의 정확한 경로
    currentIndex: Int,
    onChapterSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 다이얼로그 구현
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("챕터 목록", fontWeight = FontWeight.Bold) },
        text = {
            // 스크롤 가능한 챕터 목록
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex)
            LazyColumn(state = listState) {
                itemsIndexed(chapters) { index, chapter ->
                    Text(
                        text = "${index + 1}. ${chapter.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterSelect(index) } // 클릭 시 해당 챕터로 이동
                            .padding(vertical = 12.dp),
                        color = if (index == currentIndex) MaterialTheme.colorScheme.primary else Color.Unspecified, // 현재 챕터는 강조 표시
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
                // 현재 글자 크기 예시
                Text(
                    "가나다라",
                    fontSize = currentSize.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 슬라이더로 글자 크기 조절
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("작게", fontSize = 12.sp)
                    Slider(
                        value = currentSize.toFloat(),
                        onValueChange = { onSizeChange(it.toInt()) },
                        valueRange = 12f..28f, // 글자 크기 범위 (12sp ~ 28sp)
                        steps = 15, // (28 - 12 - 1) = 15단계로 조절
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