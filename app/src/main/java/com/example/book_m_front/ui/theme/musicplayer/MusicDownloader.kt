// MusicDownloader.kt
package com.example.book_m_front.ui.theme.musicplayer

import android.content.Context
import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.network.dto.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicDownloader @Inject constructor(
    private val context: Context
) {
    private val apiService = ApiClient.getService()

    // 음악 파일을 다운로드하고 로컬 경로 반환
    suspend fun downloadMusic(music: Music): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. 서버에서 파일 다운로드
            val response = apiService.downloadMusicFile(music.id)

            if (!response.isSuccessful || response.body() == null) {
                return@withContext Result.failure(Exception("다운로드 실패: ${response.code()}"))
            }

            // 2. 로컬 저장 경로 생성
            val musicDir = File(context.getExternalFilesDir(null), "music")
            if (!musicDir.exists()) {
                musicDir.mkdirs()
            }

            // 3. 파일명 생성 (예: music_123.mp3)
            val fileName = "music_${music.id}.mp3"
            val musicFile = File(musicDir, fileName)

            // 4. 이미 다운로드된 파일이 있으면 재사용
            if (musicFile.exists()) {
                return@withContext Result.success(musicFile.absolutePath)
            }

            // 5. 파일 저장
            response.body()!!.byteStream().use { inputStream ->
                FileOutputStream(musicFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // 6. 로컬 파일 경로 반환
            Result.success(musicFile.absolutePath)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 캐시된 파일 경로 가져오기 (다운로드 안 함)
    fun getCachedFilePath(music: Music): String? {
        val musicDir = File(context.getExternalFilesDir(null), "music")
        val musicFile = File(musicDir, "music_${music.id}.mp3")
        return if (musicFile.exists()) musicFile.absolutePath else null
    }

    // 캐시 삭제
    fun clearCache() {
        val musicDir = File(context.getExternalFilesDir(null), "music")
        musicDir.listFiles()?.forEach { it.delete() }
    }
}