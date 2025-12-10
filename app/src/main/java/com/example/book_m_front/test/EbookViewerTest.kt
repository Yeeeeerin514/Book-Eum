package com.example.book_m_front.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.book_m_front.ui.theme.ui.EbookViewerWithMusicScreen
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.book_m_front.util.SafeEpubParser

fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * ✅ 수정: Hilt 없이 테스트할 수 있는 버전
 *
 * 로컬 EPUB 파일을 EbookViewerWithMusic 컴포저블로 테스트하는 화면입니다.
 * 음악 플레이어 기능은 제외하고 EPUB 뷰어만 테스트합니다.
 */
@Composable
fun LocalEpubViewerTestScreen() {
    var realFilePath by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                realFilePath = copyUriToInternalStorage(context, uri, "selected_book.epub")

                if (realFilePath == null) {
                    Toast.makeText(context, "파일을 읽어오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "파일 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (realFilePath != null) {
            // ✅ 수정: Hilt 없이 동작하도록 간단한 뷰어만 표시
            SimpleEpubViewerForTest(
                testFilePath = realFilePath!!,
                onBackClick = {
                    realFilePath = null
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("테스트할 EPUB 파일을 선택하세요.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        filePickerLauncher.launch(arrayOf("application/epub+zip"))
                    }) {
                        Text("EPUB 파일 선택하기")
                    }
                }
            }
        }
    }
}

/**
 * ✅ 새로 추가: 음악 플레이어 없이 EPUB만 표시하는 간단한 뷰어
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleEpubViewerForTest(
    testFilePath: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var epubContent by remember { mutableStateOf<com.example.book_m_front.util.EpubContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentChapterIndex by remember { mutableStateOf(0) }
    var fontSize by remember { mutableStateOf(16) }
    var isDarkMode by remember { mutableStateOf(false) }

    // EPUB 파일 파싱
    LaunchedEffect(testFilePath) {
        isLoading = true
        errorMessage = null
        try {
            epubContent = SafeEpubParser.parseEpub(
                context,
                testFilePath
            )
        } catch (e: Exception) {
            errorMessage = "오류가 발생했습니다: ${e.message}"
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val backgroundColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    else androidx.compose.ui.graphics.Color(0xFFFFFBF5)
    val textColor = if (isDarkMode) androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    else androidx.compose.ui.graphics.Color(0xFF2C2C2C)

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = epubContent?.title ?: "로컬 테스트 책",
                            color = textColor
                        )
                        if (epubContent != null && epubContent!!.chapters.isNotEmpty()) {
                            Text(
                                text = "${currentChapterIndex + 1} / ${epubContent!!.chapters.size}",
                                color = textColor.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        androidx.compose.material3.Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "뒤로가기",
                            tint = textColor
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {
            when {
                isLoading -> {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage!!, color = androidx.compose.ui.graphics.Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("돌아가기")
                        }
                    }
                }
                epubContent != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 본문
                        Box(modifier = Modifier.weight(1f)) {
                            com.example.book_m_front.ui.theme.ui.ImprovedEbookContent(
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

                        // 네비게이션 버튼
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { if (currentChapterIndex > 0) currentChapterIndex-- },
                                enabled = currentChapterIndex > 0
                            ) {
                                Text("이전")
                            }
                            Text(
                                "${currentChapterIndex + 1} / ${epubContent!!.chapters.size}",
                                modifier = Modifier.align(Alignment.CenterVertically)
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
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocalEpubViewerTestScreenPreview() {
    LocalEpubViewerTestScreen()
}