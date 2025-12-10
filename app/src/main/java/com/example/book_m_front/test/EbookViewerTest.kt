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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.book_m_front.ui.theme.ui.EbookViewerWithMusicScreen // EbookViewerWithMusic의 실제 경로로 수정하세요
import com.example.book_m_front.ui.theme.viewmodel.EbookViewModel
import java.io.File
import java.io.FileOutputStream
import android.content.Context
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel


fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        // 앱 내부 캐시 디렉터리에 파일을 생성합니다.
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)

        // ContentResolver를 사용하여 URI로부터 InputStream을 엽니다.
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // InputStream에서 읽은 데이터를 파일에 씁니다.
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        // 생성된 파일의 절대 경로를 반환합니다.
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 로컬 EPUB 파일을 EbookViewerWithMusic 컴포저블로 띄우는 테스트용 화면입니다.
 */
@Composable
fun LocalEpubViewerTestScreen() {
    // 1. 상태 관리
    // 선택된 EPUB 파일의 경로(Uri)를 저장하는 상태 변수
    var selectedEpubUri by remember { mutableStateOf<Uri?>(null) }
    var realFilePath by remember { mutableStateOf<String?>(null) } // 1. 파일 경로를 저장할 상태 변수 변경
    val context = LocalContext.current

    // 2. 파일 선택기 (Launcher)
    // 사용자가 문서를 선택하면 그 결과(Uri)를 받아 selectedEpubUri 상태를 업데이트합니다.
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                realFilePath = copyUriToInternalStorage(context, uri, "selected_book.epub")

                if (realFilePath == null) {
                    Toast.makeText(context, "파일을 읽어오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                //selectedEpubUri = uri
                // EPUB 파일인지 간단히 확인 (더 정확한 MIME 타입 확인이 필요할 수 있음)
                /*if (uri.path?.endsWith(".epub") == true) {
                    selectedEpubUri = uri
                } else {
                    Toast.makeText(context, "EPUB 파일만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                }*/
            }
        }
    )

    // 3. UI 렌더링
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // 선택된 파일이 있으면 EbookViewerWithMusic를 보여주고, 없으면 파일 선택 버튼을 보여줍니다.
        if (realFilePath != null) {
            // EbookViewerWithMusic에 필요한 ViewModel들을 생성합니다.
            val ebookViewModel: EbookViewModel = viewModel()
            val musicPlayerViewModel: MusicPlayerViewModel = viewModel()

            // 파일 URI에서 실제 파일 경로(String)를 추출합니다.
            // EbookViewerWithMusic가 String 타입의 경로를 받는다고 가정합니다.
            val filePath = selectedEpubUri!!.toString() // Content URI를 문자열로 전달

            EbookViewerWithMusicScreen(
                bookTitle = "로컬 테스트 책",
                bookAuthor = "테스터",
                bookIsbn = "000-000-000",
                testFilePath = realFilePath!!,
                onBackClick = {
                    // 뒤로가기 버튼을 누르면 파일 선택 화면으로 돌아갑니다.
                    realFilePath = null
                },
            )
        } else {
            // 파일 선택 화면
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("테스트할 EPUB 파일을 선택하세요.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // "application/epub+zip" MIME 타입을 사용하여 EPUB 파일만 필터링합니다.
                        filePickerLauncher.launch(arrayOf("application/epub+zip"))
                    }) {
                        Text("EPUB 파일 선택하기")
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
