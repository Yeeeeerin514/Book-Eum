package com.example.book_m_front.ui.theme.musicplayer

import android.content.Context
import android.util.Log
import com.example.book_m_front.network.Api
import com.example.book_m_front.network.UnifiedFileDownloader
import com.example.book_m_front.network.dto.MusicTrack
import com.example.book_m_front.network.dto.BookPlaylistResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 통합 음악 다운로드 매니저
 *
 * MusicDownloader와 MusicDownloadManager의 기능을 통합
 * 중복 제거 및 단일 책임 원칙 준수
 */
@Singleton
class MusicDownloadService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MusicDownloadService"
    }

    /**
     * 📥 단일 음악 트랙 다운로드
     *
     * @param track 다운로드할 음악 트랙
     * @return 로컬 파일 경로 (실패 시 null)
     */
    suspend fun downloadTrack(track: MusicTrack): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. 캐시 확인
            val cachedPath = getCachedTrackPath(track.id)
            if (cachedPath != null) {
                Log.d(TAG, "캐시 적중: ${track.title} → $cachedPath")
                return@withContext Result.success(cachedPath)
            }

            // 2. 다운로드
            val localPath = UnifiedFileDownloader.downloadFile(
                context = context,
                fileId = track.id,
                fileType = UnifiedFileDownloader.FileType.MUSIC,
                apiCall = { Api.retrofitService.downloadMusicFile(track.id) }
            )

            if (localPath != null) {
                Log.d(TAG, "다운로드 완료: ${track.title} → $localPath")
                Result.success(localPath)
            } else {
                Result.failure(Exception("다운로드 실패: ${track.title}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "다운로드 오류: ${track.title}", e)
            Result.failure(e)
        }
    }

    /**
     * 📚 ISBN으로 전체 플레이리스트 다운로드
     *
     * @param isbn 책 ISBN
     * @param onFirstChapterReady 첫 챕터 다운로드 완료 콜백
     * @param onProgress 진행률 콜백 (current, total)
     * @param onComplete 전체 완료 콜백
     */
    suspend fun downloadPlaylistByIsbn(
        isbn: String,
        onFirstChapterReady: (List<String>) -> Unit = {},
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
        onComplete: () -> Unit = {}
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "플레이리스트 다운로드 시작: ISBN=$isbn")

            // 1. 플레이리스트 메타데이터 조회
            val response = Api.retrofitService.getChapterBasedPlaylist(isbn)

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
            val allLocalPaths = mutableListOf<String>()

            // 2. 첫 번째 챕터 우선 다운로드
            val firstChapter = playlist.chapters.firstOrNull()

            if (firstChapter != null) {
                Log.d(TAG, "첫 챕터 다운로드 시작: ${firstChapter.chapterTitle}")

                val firstChapterPaths = mutableListOf<String>()

                for ((index, track) in firstChapter.tracks.withIndex()) {
                    Log.d(TAG, "첫 챕터 [${index + 1}/${firstChapter.tracks.size}] 다운로드: ${track.title}")

                    val result = downloadTrack(track)

                    if (result.isSuccess) {
                        val path = result.getOrNull()!!
                        firstChapterPaths.add(path)
                        allLocalPaths.add(path)
                        downloadedCount++
                        onProgress(downloadedCount, totalTracks)
                    } else {
                        Log.w(TAG, "다운로드 실패: ${track.title}")
                    }
                }

                // 🎉 첫 챕터 완료 콜백
                Log.d(TAG, "🎉 첫 챕터 다운로드 완료! ${firstChapterPaths.size}곡 준비됨")
                withContext(Dispatchers.Main) {
                    onFirstChapterReady(firstChapterPaths)
                }
            }

            // 3. 나머지 챕터 다운로드
            val remainingChapters = playlist.chapters.drop(1)

            for ((chapterIndex, chapter) in remainingChapters.withIndex()) {
                Log.d(TAG, "챕터 [${chapterIndex + 2}/${playlist.totalChapters}] 다운로드: ${chapter.chapterTitle}")

                for (track in chapter.tracks) {
                    val result = downloadTrack(track)

                    if (result.isSuccess) {
                        allLocalPaths.add(result.getOrNull()!!)
                        downloadedCount++
                        onProgress(downloadedCount, totalTracks)
                    }
                }
            }

            // 4. 전체 완료 콜백
            Log.d(TAG, "✅ 전체 다운로드 완료! $downloadedCount/$totalTracks 곡")
            withContext(Dispatchers.Main) {
                onComplete()
            }

            Result.success(allLocalPaths)

        } catch (e: Exception) {
            Log.e(TAG, "다운로드 중 오류 발생", e)
            Result.failure(e)
        }
    }

    /**
     * 🔍 캐시된 트랙 경로 확인
     */
    fun getCachedTrackPath(musicId: String): String? {
        return UnifiedFileDownloader.getCachedFilePath(
            context = context,
            fileId = musicId,
            fileType = UnifiedFileDownloader.FileType.MUSIC
        )
    }

    /**
     * 🗑️ 모든 음악 캐시 삭제
     */
    fun clearAllCache(): Int {
        return UnifiedFileDownloader.clearCache(
            context = context,
            fileType = UnifiedFileDownloader.FileType.MUSIC
        )
    }

    /**
     * 📊 캐시 크기 계산
     */
    fun getCacheSize(): Long {
        return UnifiedFileDownloader.getCacheSize(
            context = context,
            fileType = UnifiedFileDownloader.FileType.MUSIC
        )
    }

    /**
     * 📊 포맷된 캐시 크기
     */
    fun getFormattedCacheSize(): String {
        return UnifiedFileDownloader.getFormattedCacheSize(
            context = context,
            fileType = UnifiedFileDownloader.FileType.MUSIC
        )
    }
}

/**
 * 하위 호환성을 위한 타입 별칭
 */
typealias MusicDownloader = MusicDownloadService
typealias MusicDownloadManager = MusicDownloadService