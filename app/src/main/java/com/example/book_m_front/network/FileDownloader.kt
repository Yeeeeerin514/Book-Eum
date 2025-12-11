package com.example.book_m_front.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

private const val TAG = "FileDownloader" // ✨ 이 줄 추가
/**
 * 서버에서 ISBN에 해당하는 EPUB 파일을 다운로드하여 내부 캐시에 저장하고,
 * 저장된 파일의 경로를 반환합니다.
 *
 * 이 함수는 네트워크 IO와 파일 쓰기를 수행하므로 반드시 코루틴 내부(IO Dispatcher)에서 호출되어야 합니다.
 *
 * @param context Context - 파일을 저장할 내부 캐시 디렉터리 접근에 필요합니다.
 * @param isbn 다운로드할 책의 ISBN.
 * @return 성공 시 저장된 파일의 절대 경로(String), 실패 시 null을 반환합니다.
 */
suspend fun downloadAndGetBookPath(context: Context, isbn: String): String? {
    // withContext(Dispatchers.IO)를 사용하여 이 블록을 IO 스레드에서 실행합니다.
    return withContext(Dispatchers.IO) {
        try {
            // 1. ApiService를 통해 파일 다운로드를 요청합니다.
            val response = Api.retrofitService.downloadBookFile(isbn)

            // 2. 서버 응답을 확인합니다.
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!

                // 3. 파일을 저장할 경로와 파일명을 지정합니다.
                // 앱의 내부 캐시 디렉터리를 사용하여, 앱이 삭제될 때 파일도 함께 정리되도록 합니다.
                val file = File(context.cacheDir, "$isbn.epub")

                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null

                try {
                    // 4. 응답 본문(ResponseBody)으로부터 데이터를 읽어 파일에 씁니다.
                    inputStream = responseBody.byteStream()
                    outputStream = FileOutputStream(file)

                    val buffer = ByteArray(4096) // 4KB 버퍼
                    var read: Int

                    // inputStream에서 데이터를 읽어 buffer에 저장하고, 읽은 바이트 수를 read에 할당합니다.
                    // 파일의 끝에 도달하면 -1을 반환하여 루프가 종료됩니다.
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }

                    outputStream.flush() // 버퍼에 남아있는 데이터를 파일에 모두 씁니다.

                    // 5. 성공적으로 저장된 파일의 절대 경로를 반환합니다.
                    return@withContext file.absolutePath

                } catch (e: IOException) {
                    // 파일 읽기/쓰기 중 오류 발생 시
                    e.printStackTrace()
                    return@withContext null
                } finally {
                    // 스트림을 안전하게 닫습니다.
                    inputStream?.close()
                    outputStream?.close()
                }
            } else {
                // 서버가 404, 500 등 오류 코드를 응답한 경우
                Log.e(TAG, "파일 다운로드 실패. 응답 코드: ${response.code()}, 메시지: ${response.message()}")
                return@withContext null
            }
        } catch (e: Exception) {
            // 네트워크 연결 실패 등 Retrofit 호출 자체에서 예외가 발생한 경우
            Log.e(TAG, "파일 다운로드 중 네트워크 오류 발생", e) // 예외 객체(e)도 함께 넘겨주면 스택 트레이스가 기록됨            e.printStackTrace()
            return@withContext null
        }
    }
}




/**
 * 서버에서 musicId에 해당하는 음악 파일을 다운로드하여 내부 캐시에 저장하고,
 * 저장된 파일의 경로를 반환합니다.
 *
 * @param context Context
 * @param musicId 다운로드할 음악의 고유 ID
 * @return 성공 시 저장된 파일의 절대 경로(String), 실패 시 null
 */
suspend fun downloadMusicAndGetPath(context: Context, musicId: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            // 1. ApiService를 통해 음악 파일 다운로드 요청
            val response = Api.retrofitService.downloadMusicFile(musicId)

            if (response.isSuccessful && response.body() != null) {
                // 2. 내부 캐시에 "musicId.mp3" 형식으로 파일 저장
                val file = File(context.cacheDir, "$musicId.mp3")

                // 이미 파일이 존재하면 새로 다운로드하지 않고 기존 경로를 반환 (선택적 최적화)
                if (file.exists()) {
                    return@withContext file.absolutePath
                }

                // 3. 스트림을 이용해 파일 쓰기
                response.body()!!.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // 4. 성공적으로 저장된 파일의 절대 경로 반환
                return@withContext file.absolutePath

            } else {
                println("음악 파일 다운로드 실패. 코드: ${response.code()}")
                return@withContext null
            }
        } catch (e: Exception) {
            println("음악 파일 다운로드 중 오류 발생: ${e.message}")
            return@withContext null
        }
    }
}
