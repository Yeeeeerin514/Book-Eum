package com.example.book_m_front.ui.theme.musicplayer

import android.content.Context
import android.util.Log
import com.example.book_m_front.network.Api
import com.example.book_m_front.network.dto.ChapterBasedPlaylistResponse
import com.example.book_m_front.network.dto.MusicTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 음악 다운로드 매니저
 *
 * 주요 기능:
 * 1. 플레이리스트 메타데이터 조회
 * 2. 첫 번째 챕터 우선 다운로드
 * 3. 첫 챕터 완료 시 콜백 → 즉시 재생!
 * 4. 나머지 챕터 백그라운드 다운로드
 * 5. 캐싱 (이미 다운로드된 파일 재사용)
 */
@Singleton
class MusicDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MusicDownloadManager"
    }

    private val apiService = Api.retrofitService
    private val musicDir = File(context.getExternalFilesDir(null), "music")

    init {
        // 음악 저장 디렉토리 생성
        if (!musicDir.exists()) {
            musicDir.mkdirs()
        }
    }

    /**
     * 🎯 핵심 함수: ISBN으로 플레이리스트 다운로드
     *
     * @param isbn 책 ISBN
     * @param onFirstChapterReady 첫 챕터 다운로드 완료 콜백 (즉시 재생 시작!)
     * @param onProgress 전체 진행률 콜백 (current, total)
     * @param onComplete 전체 다운로드 완료 콜백
     * @return Result<Unit>
     */
    suspend fun downloadPlaylistByIsbn(
        isbn: String,
        onFirstChapterReady: (List<String>) -> Unit,
        onProgress: (current: Int, total: Int) -> Unit,
        onComplete: () -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "플레이리스트 다운로드 시작: ISBN=$isbn")

            // 1. 플레이리스트 메타데이터 조회
            val response = apiService.getChapterBasedPlaylist(isbn)

            if (!response.isSuccessful || response.body() == null) {
                Log.e(TAG, "플레이리스트 조회 실패: ${response.code()}")
                return@withContext Result.failure(
                    Exception("플레이리스트 조회 실패: ${response.code()}")
                )
            }

            val playlist = response.body()!!
            Log.d(TAG, "플레이리스트 조회 성공: ${playlist.totalChapters}개 챕터, ${playlist.totalTracks}곡")

            var downloadedCount = 0
            val totalTracks = playlist.totalTracks

            // 2. 첫 번째 챕터 우선 다운로드 (챕터 리스트 중 첫번째 꺼 가져오기)
            val firstChapter = playlist.chapters.firstOrNull()

            if (firstChapter != null) {
                Log.d(TAG, "첫 챕터 다운로드 시작: ${firstChapter.chapterTitle} (${firstChapter.tracks.size}곡)")

                val firstChapterPaths = mutableListOf<String>()

                for ((index, track) in firstChapter.tracks.withIndex()) {
                    Log.d(TAG, "첫 챕터 [${index + 1}/${firstChapter.tracks.size}] 다운로드: ${track.title}")

                    val localPath = downloadTrack(track)

                    if (localPath != null) {
                        firstChapterPaths.add(localPath)
                        downloadedCount++
                        onProgress(downloadedCount, totalTracks)
                        Log.d(TAG, "다운로드 완료: ${track.title} → $localPath")
                    } else {
                        Log.w(TAG, "다운로드 실패: ${track.title}")
                    }
                }

                // 🎉 첫 번째 챕터 완료! → 즉시 재생 가능!
                Log.d(TAG, "🎉 첫 챕터 다운로드 완료! ${firstChapterPaths.size}곡 준비됨")
                withContext(Dispatchers.Main) {
                    onFirstChapterReady(firstChapterPaths)
                }
            }

            // 3. 나머지 챕터 다운로드 (백그라운드)
            val remainingChapters = playlist.chapters.drop(1)

            if (remainingChapters.isNotEmpty()) {
                Log.d(TAG, "나머지 ${remainingChapters.size}개 챕터 다운로드 시작 (백그라운드)")

                for ((chapterIndex, chapter) in remainingChapters.withIndex()) {
                    Log.d(TAG, "챕터 [${chapterIndex + 2}/${playlist.totalChapters}] 다운로드: ${chapter.chapterTitle}")

                    for (track in chapter.tracks) {
                        val localPath = downloadTrack(track)

                        if (localPath != null) {
                            downloadedCount++
                            onProgress(downloadedCount, totalTracks)
                        }
                    }
                }
            }

            // 4. 전체 다운로드 완료
            Log.d(TAG, "✅ 전체 다운로드 완료! $downloadedCount/$totalTracks 곡")
            withContext(Dispatchers.Main) {
                onComplete()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "다운로드 중 오류 발생", e)
            Result.failure(e)
        }
    }

    /**
     * 개별 트랙 다운로드
     *
     * @param track 다운로드할 음악 트랙
     * @return 로컬 파일 경로 (실패 시 null)
     */
    private suspend fun downloadTrack(track: MusicTrack): String? {
        return try {
            // 1. 캐시 확인
            val cachedPath = getCachedMusicPath(track.id)
            if (cachedPath != null) {
                Log.d(TAG, "캐시 적중: ${track.title} → $cachedPath")
                return cachedPath
            }

            // 2. 서버에서 다운로드
            val response = apiService.downloadMusicFile(track.id)

            if (!response.isSuccessful || response.body() == null) {
                Log.e(TAG, "다운로드 실패: ${track.title} (${response.code()})")
                return null
            }

            // 3. 파일 저장
            val file = File(musicDir, "${track.id}.mp3")

            response.body()!!.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            Log.d(TAG, "파일 저장 완료: ${file.absolutePath} (${file.length()} bytes)")
            file.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "트랙 다운로드 오류: ${track.title}", e)
            null
        }
    }

    /**
     * 캐시된 음악 파일 경로 확인
     *
     * @param musicId 음악 ID
     * @return 로컬 파일 경로 (없으면 null)
     */
    fun getCachedMusicPath(musicId: String): String? {
        val file = File(musicDir, "$musicId.mp3")
        return if (file.exists() && file.length() > 0) {
            file.absolutePath
        } else {
            null
        }
    }

    /**
     * 모든 캐시 삭제
     *
     * @return 삭제된 파일 수
     */
    fun clearAllCache(): Int {
        var count = 0
        musicDir.listFiles()?.forEach { file ->
            if (file.delete()) {
                count++
            }
        }
        Log.d(TAG, "캐시 삭제 완료: $count 개 파일")
        return count
    }

    /**
     * 캐시 크기 계산
     *
     * @return 총 캐시 크기 (바이트)
     */
    fun getCacheSize(): Long {
        var totalSize = 0L
        musicDir.listFiles()?.forEach { file ->
            totalSize += file.length()
        }
        return totalSize
    }

    /**
     * 캐시 크기 (사람이 읽기 쉬운 형식)
     *
     * @return 예: "125.5 MB"
     */
    fun getCacheSizeFormatted(): String {
        val sizeInBytes = getCacheSize()
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