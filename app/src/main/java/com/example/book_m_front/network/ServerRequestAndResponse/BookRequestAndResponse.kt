package com.example.book_m_front.network.ServerRequestAndResponse

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
//import androidx.privacysandbox.tools.core.generator.build
import com.example.book_m_front.network.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


// 서버에 파일 업로드
suspend fun uploadBookToServer(
    context: Context,
    title: String,
    author: String,
    isbn: String,
    plot: String,
    fileUri: Uri
): BookUploadResponse { //본 코드에서 result라는 객체에 저장
    return withContext(Dispatchers.IO) {
        try {
            // URI에서 파일 읽기
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: throw IOException("파일을 열 수 없습니다")

            val fileName = getFileName(context, fileUri) ?: "book.epub"
            val fileBytes = inputStream.readBytes() //서버에 e-pub파일이 그대로 저장됨.
            inputStream.close()

            // RequestBody 생성 : RequestBody - 서버로 전송할 수 있는 형태임.
            val requestFile = fileBytes.toRequestBody(
                "application/epub+zip".toMediaTypeOrNull()
            )

            val filePart = MultipartBody.Part.createFormData(
                "epubFile",     //이게 key가 되어서, 이 key에 해당하는 백엔드 변수에게 전해짐.
                fileName,     //파일 이름
                requestFile     //앞서 생성한 e-pub파일이 저장되어있는 얘.
            )
            //이 body들 모두 서버에 전송하기 위한 형태 인듯.
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val authorBody = author.toRequestBody("text/plain".toMediaTypeOrNull())
            val isbnBody = isbn.toRequestBody("text/plain".toMediaTypeOrNull())
            val plotBody = plot.toRequestBody("text/plain".toMediaTypeOrNull())

            // API 호출!!!!!!!!!!!!!!
            val response = Api.retrofitService.uploadBook(
                title = titleBody,  //다 body로 만들어서 보낸다.
                author = authorBody,
                isbn = isbnBody,
                plot = plotBody,
                file = filePart     //e-pub 파일이 저장되어있는 것!
            )

            if (response.isSuccessful) {
                response.body() ?: BookUploadResponse(false, "응답이 비어있습니다")
            } else {
                BookUploadResponse(false, "서버 오류: ${response.code()}")
            }
        } catch (e: Exception) {
            BookUploadResponse(false, "업로드 실패: ${e.message}")
        }
    }
}

// 파일명 추출 헬퍼 함수
fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
    }
    return fileName
}


// 책 다운로드 함수 : 기존에 e-pub파일 자체를 받는 거에서 -> link를 받는 걸로 수정함.
suspend fun downloadBookFromServer(
    context: Context,
    isbn: String
): File? {
    return withContext(Dispatchers.IO) {
        try {
            //1단계 : 서버에 isbn을 넘겨서 책 다운로드 링크 받기
            val bookResponse = Api.retrofitService.downloadBook(isbn)

            //실패 시
            if (bookResponse.epubFileUrl == null) {
                // 서버가 URL을 주지 않았거나, 실패 응답을 보냄
                println("서버로부터 유효한 다운로드 URL을 받지 못했습니다.")
                return@withContext null //리턴하니까..끝나는.
            }

            val downloadUrl = bookResponse.epubFileUrl // "https://.../book.epub?token=..."

            // --- 2단계: 받은 URL로 실제 파일 데이터 다운로드하기 ---
            // OkHttpClient를 사용하여 순수 GET 요청을 보냄
            val client = OkHttpClient()
            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute() // 클라우드 URL에 직접 요청

            //클라우드에서 다운받기 실패 시
            if (!response.isSuccessful || response.body == null) {
                println("클라우드 URL에서 파일을 다운로드하는 데 실패했습니다.")
                return@withContext null
            }

            // --- 3단계: 다운로드한 데이터를 파일로 저장하기 ---
            val booksDir = File(context.filesDir, "books")
            if (!booksDir.exists()) {
                booksDir.mkdirs()
            }
            val bookFile = File(booksDir, "$isbn.epub")

            response.body?.let { responseBody ->
                FileOutputStream(bookFile).use { output ->
                    responseBody.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }

            // 최종적으로 저장된 파일 객체를 반환 (아 이게 반환이구나 그렇네 근데 왜 return이라고 적는 건 안되지)
            bookFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

//바로 파일받는 옛날 코드
/*
suspend fun downloadBookFromServer(
    context: Context,
    isbn: String
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val response = Api.retrofitService.downloadBook(isbn)

            if (response.isSuccessful && response.body() != null) {
                // 앱 내부 저장소에 파일 저장
                //책 저장 폴더?생성?
                val booksDir = File(context.filesDir, "books")
                if (!booksDir.exists()) {
                    booksDir.mkdirs()
                }
                //근데 이거 이펍파일을 받는게 맞나
                val bookFile = File(booksDir, "$isbn.epub")

                response.body()?.let { responseBody ->
                    FileOutputStream(bookFile).use { output ->
                        responseBody.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                }

                bookFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}*/
