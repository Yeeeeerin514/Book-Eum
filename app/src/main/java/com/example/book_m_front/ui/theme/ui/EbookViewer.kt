package com.example.book_m_front.ui.theme.ui

import android.content.Context
import android.net.Uri
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.ui_resource.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import androidx.compose.ui.tooling.preview.Preview
import com.example.book_m_front.network.Api


/*
TODO
 isbn을 navigation을 통해 넘겨 받음.
 이걸 받아서, 이걸로 백엔드와 통신해서, 세진이가 파싱한 책 파일을 받아옴.
 그걸 띄움!
 그리고 현재 위치를 계속 추적해서, 챕터가 넘어가면 백엔드에게 알려줌.
 */

// 데이터 클래스
data class EpubContent(
    val title: String,
    val author: String,
    val chapters: List<Chapter>,
    val fullText: String
)

data class Chapter(
    val title: String,
    val content: String,
    val htmlContent: String = "" // 원본 HTML 유지
)

// EPUB 파서 - 직접 구현 (외부 라이브러리 없이)
object SimpleEpubParser {
    suspend fun parseEpub(context: Context, filePath: String): EpubContent {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = if (filePath.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(filePath))
                        ?: throw Exception("파일을 열 수 없습니다")
                } else {
                    File(filePath).inputStream()
                }

                // EPUB은 ZIP 파일 형식
                val zipInputStream = ZipInputStream(inputStream)
                var entry: ZipEntry?

                var title = "제목 없음"
                var author = "저자 미상"
                val chapters = mutableListOf<Chapter>()
                val contentFiles = mutableListOf<Pair<String, String>>()
                var opfContent = ""

                // ZIP 파일 내용 읽기
                while (zipInputStream.nextEntry.also { entry = it } != null) {
                    val entryName = entry!!.name

                    when {
                        // 메타데이터 파일 (content.opf)
                        entryName.endsWith(".opf") -> {
                            opfContent = readInputStream(zipInputStream)
                        }
                        // HTML/XHTML 콘텐츠 파일
                        entryName.endsWith(".html") ||
                                entryName.endsWith(".xhtml") ||
                                entryName.endsWith(".htm") -> {
                            val content = readInputStream(zipInputStream)
                            contentFiles.add(Pair(entryName, content))
                        }
                    }
                    zipInputStream.closeEntry()
                }

                // OPF에서 메타데이터 추출
                if (opfContent.isNotEmpty()) {
                    title = extractTag(opfContent, "dc:title") ?: title
                    author = extractTag(opfContent, "dc:creator") ?: author
                }

                // 콘텐츠 파일을 챕터로 변환
                contentFiles.forEachIndexed { index, (fileName, htmlContent) ->
                    val chapterTitle = extractTitle(htmlContent) ?: "챕터 ${index + 1}"
                    val plainText = htmlToPlainText(htmlContent)

                    if (plainText.isNotBlank() && plainText.length > 50) {
                        chapters.add(
                            Chapter(
                                title = chapterTitle,
                                content = plainText,
                                htmlContent = htmlContent
                            )
                        )
                    }
                }

                // 챕터가 없으면 전체를 하나의 챕터로
                if (chapters.isEmpty()) {
                    chapters.add(
                        Chapter(
                            title = "본문",
                            content = "EPUB 파일 내용을 추출할 수 없습니다.",
                            htmlContent = ""
                        )
                    )
                }

                EpubContent(
                    title = title,
                    author = author,
                    chapters = chapters,
                    fullText = chapters.joinToString("\n\n") { it.content }
                )
            } catch (e: Exception) {
                throw Exception("EPUB 파일 파싱 실패: ${e.message}")
            }
        }
    }

    private fun readInputStream(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        return reader.readText()
    }

    private fun extractTag(xml: String, tagName: String): String? {
        val pattern = "<$tagName[^>]*>([^<]+)</$tagName>".toRegex()
        return pattern.find(xml)?.groupValues?.get(1)?.trim()
    }

    private fun extractTitle(html: String): String? {
        // <title> 태그에서 추출
        var title = extractTag(html, "title")

        // <h1>, <h2> 태그에서 추출
        if (title.isNullOrBlank()) {
            title = extractTag(html, "h1") ?: extractTag(html, "h2")
        }

        return title?.take(100) // 최대 100자
    }

    private fun htmlToPlainText(html: String): String {
        return html
            // 스크립트와 스타일 제거
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            // 태그 제거
            .replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("<p[^>]*>"), "\n")
            .replace(Regex("</p>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            // HTML 엔티티 변환
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            // 공백 정리
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}

// TXT 파일 파서
object SimpleTxtParser {
    suspend fun parseTxt(context: Context, filePath: String): EpubContent {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = if (filePath.startsWith("content://")) {
                    context.contentResolver.openInputStream(Uri.parse(filePath))
                        ?: throw Exception("파일을 열 수 없습니다")
                } else {
                    File(filePath).inputStream()
                }

                val content = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    .readText()

                // 파일명을 제목으로
                val fileName = if (filePath.startsWith("content://")) {
                    "텍스트 파일"
                } else {
                    File(filePath).nameWithoutExtension
                }

                // 내용을 적당한 크기로 챕터 분할 (5000자 단위)
                val chapters = mutableListOf<Chapter>()
                val chunkSize = 5000
                var startIndex = 0

                while (startIndex < content.length) {
                    val endIndex = minOf(startIndex + chunkSize, content.length)
                    val chunkContent = content.substring(startIndex, endIndex)

                    chapters.add(
                        Chapter(
                            title = "Part ${chapters.size + 1}",
                            content = chunkContent
                        )
                    )
                    startIndex = endIndex
                }

                if (chapters.isEmpty()) {
                    chapters.add(
                        Chapter(
                            title = "본문",
                            content = content
                        )
                    )
                }

                EpubContent(
                    title = fileName,
                    author = "알 수 없음",
                    chapters = chapters,
                    fullText = content
                )
            } catch (e: Exception) {
                throw Exception("TXT 파일 파싱 실패: ${e.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookViewerScreen(
    bookTitle: String,
    bookAuthor: String,
    bookIsbn: String,
    bookFilePath: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var epubContent by remember { mutableStateOf<EpubContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showChapterList by remember { mutableStateOf(false) }
    var currentChapterIndex by remember { mutableStateOf(0) }

    // 폰트 크기 조절
    var fontSize by remember { mutableStateOf(16) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    // 배경색 모드
    var isDarkMode by remember { mutableStateOf(false) }

    // 음악 관련
    var showMusicPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var playlist by remember { mutableStateOf<List<Music>>(emptyList()) }
    var currentTrackIndex by remember { mutableStateOf(0) }
    var isLoadingMusic by remember { mutableStateOf(false) }
    var musicError by remember { mutableStateOf<String?>(null) }

    val currentTrack = playlist.getOrNull(currentTrackIndex)

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
                        // 기본적으로 EPUB로 시도
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
            isLoadingMusic = true
            musicError = null
            try {
                playlist = fetchPlaylistFromBackend(bookIsbn)
                if (playlist.isNotEmpty()) {
                    currentTrackIndex = 0
                    isPlaying = true
                }
            } catch (e: Exception) {
                musicError = "음악을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                isLoadingMusic = false
            }
        }
    }

    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFFBF5)
    val textColor = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF2C2C2C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
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
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "챕터 목록",
                            tint = textColor
                        )
                    }
                    IconButton(onClick = { showFontSizeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = textColor
                        )
                    }
                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.Star else Icons.Default.Face,
                            contentDescription = "테마 변경",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AppColors.DeepGreen)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("책을 불러오는 중...", color = textColor)
                            }
                        }
                    }
                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "오류 발생",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "알 수 없는 오류",
                                    color = textColor.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
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
            if (epubContent != null && epubContent!!.chapters.size > 1) {
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
                            onClick = { if (currentChapterIndex > 0) currentChapterIndex-- },
                            enabled = currentChapterIndex > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.DeepGreen
                            )
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전")
                            Text("이전")
                        }

                        Text(
                            text = epubContent!!.chapters.getOrNull(currentChapterIndex)?.title ?: "",
                            color = textColor,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )

                        Button(
                            onClick = {
                                if (currentChapterIndex < epubContent!!.chapters.size - 1) {
                                    currentChapterIndex++
                                }
                            },
                            enabled = currentChapterIndex < epubContent!!.chapters.size - 1,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.DeepGreen
                            )
                        ) {
                            Text("다음")
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음")
                        }
                    }
                }
            }
        }

        // 음악 로딩 중
        if (isLoadingMusic) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.DeepGreen)
            }
        }

        // 음악 플레이어
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom
        )
        {
            AnimatedVisibility(
                visible = showMusicPlayer && playlist.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                //modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                MusicPlayerPanel(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    onPlayPauseClick = { isPlaying = !isPlaying },
                    onPreviousClick = {
                        if (currentTrackIndex > 0) {
                            currentTrackIndex--
                            isPlaying = true
                        }
                    },
                    onNextClick = {
                        if (currentTrackIndex < playlist.size - 1) {
                            currentTrackIndex++
                            isPlaying = true
                        }
                    },
                    backgroundColor = AppColors.DeepGreen
                )
            }
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
fun ImprovedEbookContent(
    chapter: Chapter?,
    fontSize: Int,
    textColor: Color,
    backgroundColor: Color,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 3) {
                            onPreviousChapter()
                        } else if (offset.x > size.width * 2 / 3) {
                            onNextChapter()
                        }
                    }
                )
            }
    ) {
        if (chapter != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Text(
                    text = chapter.title,
                    fontSize = (fontSize + 4).sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = chapter.content,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.75).sp,
                    color = textColor,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}

@Composable
fun ChapterListDialog(
    chapters: List<Chapter>,
    currentIndex: Int,
    onChapterSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("챕터 목록") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                chapters.forEachIndexed { index, chapter ->
                    TextButton(
                        onClick = { onChapterSelect(index) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${index + 1}. ${chapter.title}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            fontWeight = if (index == currentIndex)
                                FontWeight.Bold else FontWeight.Normal,
                            color = if (index == currentIndex)
                                AppColors.DeepGreen else Color.Black
                        )
                    }
                    if (index < chapters.size - 1) {
                        HorizontalDivider()
                    }
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
        title = { Text("폰트 크기") },
        text = {
            Column {
                Text("크기: ${currentSize}sp")
                Slider(
                    value = currentSize.toFloat(),
                    onValueChange = { onSizeChange(it.toInt()) },
                    valueRange = 12f..24f,
                    steps = 11
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}

@Composable
fun MusicPlayerPanel(
    track: Music?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
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

            HorizontalDivider(
                color = Color.White.copy(alpha = 1.0f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (track != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${track.artist} | ${track.album}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPreviousClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = onPlayPauseClick,
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = onNextClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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

suspend fun fetchPlaylistFromBackend(isbn: String): List<Music> {
    // TODO: 실제 API 호출 구현
    //return Api.retrofitService.getPlaylist(isbn).playlist

    // 임시 더미 데이터
    return listOf(
        Music(
            id = "1",
            title = "Sample Song 1",
            artist = "Artist A",
            album = "Album X",
            albumArtUrl = "",
            audioUrl = "https://example.com/song1.mp3"
        ),
        Music(
            id = "2",
            title = "Sample Song 2",
            artist = "Artist B",
            album = "Album Y",
            albumArtUrl = "",
            audioUrl = "https://example.com/song2.mp3"
        )
    )
}




// ... 기존 코드의 마지막 부분 ...

// --- 아래 코드를 파일 맨 끝에 추가하세요 ---

// 가짜 데이터 및 함수 (Preview용)
private val sampleEpubContent = EpubContent(
    title = "어린왕자",
    author = "앙투안 드 생텍쥐페리",
    chapters = listOf(
        Chapter("제 1장", "어른들은 숫자를 좋아한다. 새로 사귄 친구 이야기를 어른들에게 할 때면, 그들은 가장 중요한 것은 절대 묻는 법이 없다..."),
        Chapter("제 2장", "그래서 나는 다른 직업을 선택해야 했고, 비행기 조종하는 법을 배웠다. 나는 세계의 거의 모든 곳을 날아다녔다...")
    ),
    fullText = "전체 텍스트..."
)

private val samplePlaylist = listOf(
    Music("1", "B 612", "오왠(O.WHEN)", "어린왕자 OST", "s","asd"),
    Music("2", "너를 만나", "폴킴", "너를 만나", "s", "Asd")
)

// 1. 책을 불러오는 중인 상태 미리보기
@Preview(showBackground = true, name = "Loading State")
@Composable
fun EbookViewerPreview_Loading() {
    MaterialTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                CircularProgressIndicator(color = AppColors.DeepGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("책을 불러오는 중...", color = Color.Black)
            }
        }
    }
}

// 2. 오류가 발생한 상태 미리보기
@Preview(showBackground = true, name = "Error State")
@Composable
fun EbookViewerPreview_Error() {
    MaterialTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "오류 발생",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "EPUB 파일 파싱 실패: 파일을 열 수 없습니다",
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// 3. 기본 뷰어 상태 미리보기 (라이트 모드)
@Preview(showBackground = true, name = "Ebook Viewer - Light Mode")
@Composable
fun EbookViewerPreview_LightMode() {
    // 실제 Composable을 호출하여 프리뷰를 생성합니다.
    // 단, LaunchedEffect의 로직은 프리뷰에서 실행되지 않으므로,
    // 초기 상태를 직접 제어하는 것이 더 정확한 UI를 보여줄 수 있습니다.
    // 여기서는 기본 UI 구조를 확인하는 용도로 사용합니다.
    EbookViewerScreen(
        bookTitle = "어린왕자",
        bookAuthor = "생텍쥐페리",
        bookIsbn = "12345",
        bookFilePath = "fake/path/to/book.epub",
        onBackClick = {}
    )
    MusicPlayerPanel(
        track = samplePlaylist.first(),
        isPlaying = true,
        onPlayPauseClick = {},
        onPreviousClick = {},
        onNextClick = {},
        backgroundColor = Color(0xFF333333),
        //modifier = Modifier.align(Alignment.BottomCenter)
    )
}


// 4. 음악 플레이어가 보이는 상태 미리보기 (다크 모드)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Viewer with Music - Dark Mode")
@Composable
fun EbookViewerPreview_WithMusicPlayer() {
    val backgroundColor = Color(0xFF1A1A1A)
    val textColor = Color(0xFFE0E0E0)

    MaterialTheme {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)) {
            // 상단바
            TopAppBar(
                title = { Text(sampleEpubContent.title, color = textColor) },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, "", tint = textColor) }},
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.List, "", tint = textColor) }},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
            // 본문
            Column(modifier = Modifier.padding(top = 64.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    text = sampleEpubContent.chapters[0].content,
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 28.sp
                )
            }

            // 음악 플레이어
            MusicPlayerPanel(
                track = samplePlaylist.first(),
                isPlaying = true,
                onPlayPauseClick = {},
                onPreviousClick = {},
                onNextClick = {},
                backgroundColor = Color(0xFF333333),
                //modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// 음악 플레이어 패널 Composable이 private이 아니어야 Preview에서 사용 가능합니다.
// 만약 private이라면 아래와 같이 임시로 하나 만들어줍니다.
/*
@Composable
private fun MusicPlayerPanel(
    track: Music?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(track.artist, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.SkipPrevious, "", tint = Color.White)
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.SkipNext, "", tint = Color.White)
            }
        }
    }
}
*/
