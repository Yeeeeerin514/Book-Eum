package com.example.book_m_front.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 📦 통합 파일 다운로더 (강화된 디버깅 버전)
 *
 * 책(EPUB)과 음악(MP3) 다운로드를 하나의 클래스로 통합
 */
object UnifiedFileDownloader {

    private const val TAG = "UnifiedFileDownloader"
    private const val BUFFER_SIZE = 8192 // 8KB 버퍼

    /**
     * 파일 타입 정의
     */
    enum class FileType(val extension: String, val subdirectory: String) {
        EPUB("epub", "books"),
        MUSIC("mp3", "music")
    }

    /**
     * 통합 다운로드 함수 (상세 디버깅 버전)
     */
    suspend fun downloadFile(
        context: Context,
        fileId: String,
        fileType: FileType,
        apiCall: suspend () -> retrofit2.Response<okhttp3.ResponseBody>
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📥 다운로드 시작")
            Log.d(TAG, "   파일 ID: $fileId")
            Log.d(TAG, "   파일 타입: ${fileType.name}")

            // 1. 캐시 확인
            val cachedPath = getCachedFilePath(context, fileId, fileType)
            if (cachedPath != null) {
                val file = File(cachedPath)
                Log.d(TAG, "✅ 캐시 적중!")
                Log.d(TAG, "   경로: $cachedPath")
                Log.d(TAG, "   파일 크기: ${file.length()} bytes")
                Log.d(TAG, "========================================")
                return@withContext cachedPath
            }

            Log.d(TAG, "📡 서버에서 다운로드 시작...")

            // 2. API 호출
            val response = try {
                apiCall()
            } catch (e: Exception) {
                Log.e(TAG, "❌ API 호출 실패", e)
                Log.e(TAG, "   원인: ${e.message}")
                Log.e(TAG, "========================================")
                return@withContext null
            }

            Log.d(TAG, "📡 서버 응답 수신")
            Log.d(TAG, "   응답 코드: ${response.code()}")
            Log.d(TAG, "   응답 성공: ${response.isSuccessful}")
            Log.d(TAG, "   응답 메시지: ${response.message()}")

            if (!response.isSuccessful) {
                Log.e(TAG, "❌ 다운로드 실패: HTTP ${response.code()}")
                Log.e(TAG, "   에러 메시지: ${response.message()}")

                // 에러 바디가 있으면 출력
                try {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        Log.e(TAG, "   에러 상세: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "   에러 바디 읽기 실패", e)
                }

                Log.d(TAG, "========================================")
                return@withContext null
            }

            if (response.body() == null) {
                Log.e(TAG, "❌ 응답 바디가 null입니다!")
                Log.d(TAG, "========================================")
                return@withContext null
            }

            val contentLength = response.body()?.contentLength() ?: -1L
            Log.d(TAG, "📊 파일 정보")
            Log.d(TAG, "   Content-Length: $contentLength bytes")
            Log.d(TAG, "   Content-Type: ${response.headers()["Content-Type"]}")

            // 3. 파일 저장
            val file = getFileDestination(context, fileId, fileType)
            Log.d(TAG, "💾 저장 위치: ${file.absolutePath}")

            // 부모 디렉토리 생성
            val parentCreated = file.parentFile?.mkdirs() ?: false
            if (parentCreated) {
                Log.d(TAG, "📁 디렉토리 생성: ${file.parentFile?.absolutePath}")
            }

            // 스트림을 사용한 파일 쓰기
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = response.body()!!.byteStream()
                outputStream = FileOutputStream(file)

                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalBytes = 0L
                var lastLogTime = System.currentTimeMillis()

                Log.d(TAG, "📥 파일 쓰기 시작...")

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead

                    // 1초마다 진행률 로그
                    val now = System.currentTimeMillis()
                    if (now - lastLogTime > 1000) {
                        val progress = if (contentLength > 0) {
                            (totalBytes * 100 / contentLength).toInt()
                        } else {
                            -1
                        }

                        if (progress >= 0) {
                            Log.d(TAG, "   진행: $totalBytes / $contentLength bytes ($progress%)")
                        } else {
                            Log.d(TAG, "   진행: $totalBytes bytes")
                        }

                        lastLogTime = now
                    }
                }

                outputStream.flush()

                Log.d(TAG, "✅ 다운로드 완료!")
                Log.d(TAG, "   최종 크기: $totalBytes bytes")
                Log.d(TAG, "   저장 경로: ${file.absolutePath}")

                // 파일 검증
                if (file.exists()) {
                    Log.d(TAG, "✅ 파일 검증 성공")
                    Log.d(TAG, "   파일 존재: ${file.exists()}")
                    Log.d(TAG, "   파일 크기: ${file.length()} bytes")
                    Log.d(TAG, "   읽기 가능: ${file.canRead()}")
                } else {
                    Log.e(TAG, "❌ 파일이 생성되지 않았습니다!")
                }

                Log.d(TAG, "========================================")
                return@withContext file.absolutePath

            } catch (e: IOException) {
                Log.e(TAG, "❌ 파일 쓰기 오류", e)
                Log.e(TAG, "   원인: ${e.message}")
                Log.e(TAG, "   스택: ${e.stackTraceToString()}")

                // 불완전한 파일 삭제
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(TAG, "🗑️ 불완전한 파일 삭제: $deleted")
                }

                Log.d(TAG, "========================================")
                return@withContext null
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                    Log.d(TAG, "🔒 스트림 닫기 완료")
                } catch (e: Exception) {
                    Log.e(TAG, "스트림 닫기 오류", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 다운로드 중 예외 발생", e)
            Log.e(TAG, "   타입: ${e.javaClass.simpleName}")
            Log.e(TAG, "   메시지: ${e.message}")
            Log.e(TAG, "   스택: ${e.stackTraceToString()}")
            Log.d(TAG, "========================================")
            return@withContext null
        }
    }

    /**
     * 캐시된 파일 경로 확인
     */
    fun getCachedFilePath(
        context: Context,
        fileId: String,
        fileType: FileType
    ): String? {
        val file = getFileDestination(context, fileId, fileType)
        return if (file.exists() && file.length() > 0) {
            file.absolutePath
        } else {
            null
        }
    }

    /**
     * 파일 저장 위치 결정
     */
    private fun getFileDestination(
        context: Context,
        fileId: String,
        fileType: FileType
    ): File {
        val externalFilesDir = context.getExternalFilesDir(null)
        Log.d(TAG, "📂 앱 외부 저장소: ${externalFilesDir?.absolutePath}")

        val directory = File(externalFilesDir, fileType.subdirectory)
        val file = File(directory, "$fileId.${fileType.extension}")

        Log.d(TAG, "📂 파일 경로 구성:")
        Log.d(TAG, "   디렉토리: ${directory.absolutePath}")
        Log.d(TAG, "   파일명: $fileId.${fileType.extension}")

        return file
    }

    /**
     * 특정 타입의 모든 캐시 삭제
     */
    fun clearCache(context: Context, fileType: FileType): Int {
        val directory = File(context.getExternalFilesDir(null), fileType.subdirectory)
        var count = 0

        Log.d(TAG, "🗑️ 캐시 삭제 시작: ${fileType.name}")
        Log.d(TAG, "   디렉토리: ${directory.absolutePath}")

        directory.listFiles()?.forEach { file ->
            if (file.delete()) {
                count++
                Log.d(TAG, "   삭제: ${file.name}")
            }
        }

        Log.d(TAG, "✅ ${fileType.name} 캐시 삭제 완료: $count 개 파일")
        return count
    }

    /**
     * 특정 타입의 캐시 크기 계산
     */
    fun getCacheSize(context: Context, fileType: FileType): Long {
        val directory = File(context.getExternalFilesDir(null), fileType.subdirectory)
        var totalSize = 0L

        directory.listFiles()?.forEach { file ->
            totalSize += file.length()
        }

        return totalSize
    }

    /**
     * 사람이 읽기 쉬운 캐시 크기 포맷
     */
    fun getFormattedCacheSize(context: Context, fileType: FileType): String {
        val sizeInBytes = getCacheSize(context, fileType)

        return when {
            sizeInBytes < 1024 -> "$sizeInBytes B"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            sizeInBytes < 1024 * 1024 * 1024 -> {
                val mb = sizeInBytes / (1024.0 * 1024.0)
                String.format("%.1f MB", mb)
            }
            else -> {
                val gb = sizeInBytes / (1024.0 * 1024.0 * 1024.0)
                String.format("%.2f GB", gb)
            }
        }
    }
}

/**
 * 📚 책 다운로드 헬퍼 함수 (하위 호환성 유지)
 */
suspend fun downloadAndGetBookPath(context: Context, isbn: String): String? {
    return UnifiedFileDownloader.downloadFile(
        context = context,
        fileId = isbn,
        fileType = UnifiedFileDownloader.FileType.EPUB,
        apiCall = { Api.retrofitService.downloadBookFile(isbn) }
    )
}

/**
 * 🎵 음악 다운로드 헬퍼 함수 (하위 호환성 유지)
 */
suspend fun downloadMusicAndGetPath(context: Context, musicId: String): String? {
    return UnifiedFileDownloader.downloadFile(
        context = context,
        fileId = musicId,
        fileType = UnifiedFileDownloader.FileType.MUSIC,
        apiCall = { Api.retrofitService.downloadMusicFile(musicId) }
    )
}