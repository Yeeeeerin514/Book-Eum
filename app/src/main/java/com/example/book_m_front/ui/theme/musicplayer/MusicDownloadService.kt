package com.example.book_m_front.ui.theme.musicplayer

import android.content.Context
import android.util.Log
import com.example.book_m_front.network.Api
import com.example.book_m_front.network.UnifiedFileDownloader
import com.example.book_m_front.network.dto.MusicTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 통합 음악 다운로드 매니저
 */
@Singleton
class MusicDownloadService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MusicDownloadService"
    }

    /**
     * 다운로드된 트랙 정보 (경로 + 메타데이터)
     */
    data class DownloadedTrack(
        val localPath: String,
        val metadata: MusicTrack
    )

    /**
     * 📥 단일 음악 트랙 다운로드
     */
    suspend fun downloadTrack(track: MusicTrack): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎵 다운로드 요청: ${track.title} (ID: ${track.id})")

            // 1. 캐시 확인
            val cachedPath = getCachedTrackPath(track.id)
            if (cachedPath != null) {
                Log.d(TAG, "✅ 캐시 적중: ${track.title}")
                Log.d(TAG, "   📂 경로: $cachedPath")

                // 파일 검증
                val file = java.io.File(cachedPath)
                Log.d(TAG, "   📊 파일 존재: ${file.exists()}")
                Log.d(TAG, "   📊 파일 크기: ${file.length()} bytes")
                Log.d(TAG, "   📊 읽기 가능: ${file.canRead()}")

                return@withContext Result.success(cachedPath)
            }

            // 2. 다운로드
            Log.d(TAG, "📥 새로 다운로드 시작: ${track.title}")
            val localPath = UnifiedFileDownloader.downloadFile(
                context = context,
                fileId = track.id,
                fileType = UnifiedFileDownloader.FileType.MUSIC,
                apiCall = { Api.retrofitService.downloadMusicFile(track.id) }
            )

            if (localPath != null) {
                // 다운로드 후 검증
                val file = java.io.File(localPath)
                Log.d(TAG, "✅ 다운로드 완료: ${track.title}")
                Log.d(TAG, "   📂 경로: $localPath")
                Log.d(TAG, "   📊 파일 존재: ${file.exists()}")
                Log.d(TAG, "   📊 파일 크기: ${file.length()} bytes")
                Log.d(TAG, "   📊 읽기 가능: ${file.canRead()}")

                if (!file.exists() || file.length() == 0L) {
                    Log.e(TAG, "❌ 다운로드된 파일이 유효하지 않습니다!")
                    return@withContext Result.failure(Exception("파일이 유효하지 않습니다"))
                }

                Result.success(localPath)
            } else {
                Log.e(TAG, "❌ 다운로드 실패: ${track.title}")
                Result.failure(Exception("다운로드 실패: ${track.title}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 다운로드 오류: ${track.title}", e)
            Result.failure(e)
        }
    }

    /**
     * 📚 ISBN으로 전체 플레이리스트 다운로드 (메타데이터 포함)
     *
     * @param isbn 책 ISBN
     * @param onFirstChapterReady 첫 챕터 다운로드 완료 콜백 (경로 + 메타데이터)
     * @param onProgress 진행률 콜백 (current, total)
     * @param onComplete 전체 완료 콜백 (모든 트랙 정보)
     */
    suspend fun downloadPlaylistByIsbn(
        isbn: String,
        onFirstChapterReady: (List<DownloadedTrack>) -> Unit = {},
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
        onComplete: (List<DownloadedTrack>) -> Unit = {}
    ): Result<List<DownloadedTrack>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📖 플레이리스트 다운로드 시작: ISBN=$isbn")

            // 1. 플레이리스트 메타데이터 조회
            val response = Api.retrofitService.getChapterBasedPlaylist(isbn)

            if (response.body() == null) {
                Log.e(TAG, "❌ 플레이리스트 조회 실패: ${response.code()}")
                return@withContext Result.failure(
                    Exception("플레이리스트 조회 실패: ${response.code()}")
                )
            }

            val playlist = response.body()!!
            Log.d(TAG, "✅ 플레이리스트 조회 성공: ${playlist.totalChapters}개 챕터, ${playlist.totalMusic}곡")

            var downloadedCount = 0
            val totalTracks = playlist.totalMusic
            val allDownloadedTracks = mutableListOf<DownloadedTrack>()

            // 2. 첫 번째 챕터 우선 다운로드
            val firstChapter = playlist.chapterPlaylist.firstOrNull()

            if (firstChapter != null) {
                Log.d(TAG, "📥 첫 챕터 다운로드 시작: ${firstChapter.chapterNum}")

                val firstChapterTracks = mutableListOf<DownloadedTrack>()

                for ((index, track) in firstChapter.musics.withIndex()) {
                    Log.d(TAG, "   [${index + 1}/${firstChapter.musics.size}] ${track.title} - ${track.artist}")

                    val result = downloadTrack(track)

                    if (result.isSuccess) {
                        val path = result.getOrNull()!!
                        val downloadedTrack = DownloadedTrack(path, track)

                        firstChapterTracks.add(downloadedTrack)
                        allDownloadedTracks.add(downloadedTrack)
                        downloadedCount++
                        onProgress(downloadedCount, totalTracks)
                    } else {
                        Log.w(TAG, "⚠️ 다운로드 실패: ${track.title}")
                    }
                }

                // 🎉 첫 챕터 완료 콜백
                Log.d(TAG, "🎉 첫 챕터 다운로드 완료! ${firstChapterTracks.size}곡 준비됨")
                withContext(Dispatchers.Main) {
                    onFirstChapterReady(firstChapterTracks)
                }
            }

            // 3. 나머지 챕터 다운로드
            val remainingChapters = playlist.chapterPlaylist.drop(1)

            for ((chapterIndex, chapter) in remainingChapters.withIndex()) {
                Log.d(TAG, "📥 챕터 [${chapterIndex + 2}/${playlist.totalChapters}] 다운로드: ${chapter.chapterNum}")

                for (track in chapter.musics) {
                    val result = downloadTrack(track)

                    if (result.isSuccess) {
                        val downloadedTrack = DownloadedTrack(result.getOrNull()!!, track)
                        allDownloadedTracks.add(downloadedTrack)
                        downloadedCount++
                        onProgress(downloadedCount, totalTracks)
                    }
                }
            }

            // 4. 전체 완료 콜백
            Log.d(TAG, "✅ 전체 다운로드 완료! $downloadedCount/$totalTracks 곡")
            Log.d(TAG, "📂 다운로드된 곡 목록:")
            allDownloadedTracks.forEachIndexed { index, track ->
                Log.d(TAG, "   [$index] ${track.metadata.title} - ${track.metadata.artist}")
            }

            withContext(Dispatchers.Main) {
                onComplete(allDownloadedTracks)
            }

            Result.success(allDownloadedTracks)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 다운로드  중 오류 발생", e)
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