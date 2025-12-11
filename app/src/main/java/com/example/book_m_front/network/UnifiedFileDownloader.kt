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
 * 📦 통합 파일 다운로더
 *
 * 책(EPUB)과 음악(MP3) 다운로드를 하나의 클래스로 통합
 * 중복 코드 제거 및 재사용성 향상
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
     * 통합 다운로드 함수
     *
     * @param context Context
     * @param fileId 파일 식별자 (ISBN 또는 Music ID)
     * @param fileType 파일 타입
     * @param apiCall API 호출 람다
     * @return 성공 시 파일 경로, 실패 시 null
     */
    suspend fun downloadFile(
        context: Context,
        fileId: String,
        fileType: FileType,
        apiCall: suspend () -> retrofit2.Response<okhttp3.ResponseBody>
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 1. 캐시 확인
            val cachedPath = getCachedFilePath(context, fileId, fileType)
            if (cachedPath != null) {
                Log.d(TAG, "캐시 적중: $fileId → $cachedPath")
                return@withContext cachedPath
            }

            // 2. API 호출
            val response = apiCall()

            if (!response.isSuccessful || response.body() == null) {
                Log.e(TAG, "다운로드 실패: $fileId (코드: ${response.code()})")
                return@withContext null
            }

            // 3. 파일 저장
            val file = getFileDestination(context, fileId, fileType)

            // 부모 디렉토리 생성
            file.parentFile?.mkdirs()

            // 스트림을 사용한 파일 쓰기
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                inputStream = response.body()!!.byteStream()
                outputStream = FileOutputStream(file)

                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                var totalBytes = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }

                outputStream.flush()

                Log.d(TAG, "다운로드 완료: ${file.absolutePath} ($totalBytes bytes)")
                return@withContext file.absolutePath

            } catch (e: IOException) {
                Log.e(TAG, "파일 쓰기 오류: $fileId", e)
                file.delete() // 불완전한 파일 삭제
                return@withContext null
            } finally {
                inputStream?.close()
                outputStream?.close()
            }

        } catch (e: Exception) {
            Log.e(TAG, "다운로드 중 오류: $fileId", e)
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
        val directory = File(context.getExternalFilesDir(null), fileType.subdirectory)
        return File(directory, "$fileId.${fileType.extension}")
    }

    /**
     * 특정 타입의 모든 캐시 삭제
     */
    fun clearCache(context: Context, fileType: FileType): Int {
        val directory = File(context.getExternalFilesDir(null), fileType.subdirectory)
        var count = 0

        directory.listFiles()?.forEach { file ->
            if (file.delete()) {
                count++
            }
        }

        Log.d(TAG, "${fileType.name} 캐시 삭제 완료: $count 개 파일")
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