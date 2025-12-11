package com.example.book_m_front.ui.theme.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.musicplayer.MusicController
import com.example.book_m_front.ui.theme.musicplayer.MusicDownloadManager
import com.example.book_m_front.ui.theme.musicplayer.MusicRepository
import com.example.book_m_front.ui.theme.musicplayer.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🎵 음악 재생을 관리하는 통합 ViewModel (최종 수정)
 *
 * 모든 suspend 함수 호출을 viewModelScope.launch로 래핑
 */
@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository,
    private val musicDownloadManager: MusicDownloadManager
) : ViewModel() {

    companion object {
        private const val TAG = "MusicPlayerViewModel"
    }

    // ========================================
    // MusicController의 상태들을 UI에 노출
    // ========================================

    val playerState = musicController.playerState
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration
    val isPlaying = musicController.isPlaying
    val currentTrack = musicController.currentMusic

    // ========================================
    // 플레이리스트 관리
    // ========================================

    private val _playlist = MutableStateFlow<List<Music>>(emptyList())
    val playlist: StateFlow<List<Music>> = _playlist.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ========================================
    // 다운로드 관련 상태
    // ========================================

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _firstChapterReady = MutableStateFlow(false)
    val firstChapterReady: StateFlow<Boolean> = _firstChapterReady.asStateFlow()

    private val _localPlaylistPaths = MutableStateFlow<List<String>>(emptyList())
    val localPlaylistPaths: StateFlow<List<String>> = _localPlaylistPaths.asStateFlow()

    // ========================================
    // 🎯 핵심 함수: ISBN으로 플레이리스트 다운로드 및 재생
    // ========================================

    fun loadAndPlayPlaylist(isbn: String) {
        viewModelScope.launch {
            Log.d(TAG, "플레이리스트 로드 시작: ISBN=$isbn")

            _isDownloading.value = true
            _downloadProgress.value = 0f
            _firstChapterReady.value = false
            _errorMessage.value = null

            val allLocalPaths = mutableListOf<String>()

            try {
                val result = musicDownloadManager.downloadPlaylistByIsbn(
                    isbn = isbn,

                    onFirstChapterReady = { localPaths ->
                        Log.d(TAG, "🎉 첫 챕터 준비 완료: ${localPaths.size}곡")
                        _firstChapterReady.value = true
                        allLocalPaths.addAll(localPaths)

                        if (localPaths.isNotEmpty()) {
                            playLocalFile(localPaths.first())   //viewmodel에 있는 함수
                            Log.d(TAG, "🎵 재생 시작: ${localPaths.first()}")
                        }
                    },

                    onProgress = { current, total ->
                        val progress = current.toFloat() / total.toFloat()
                        _downloadProgress.value = progress
                        Log.d(TAG, "다운로드 진행: $current/$total (${(progress * 100).toInt()}%)")
                    },

                    onComplete = {
                        _isDownloading.value = false
                        _downloadProgress.value = 1f
                        _localPlaylistPaths.value = allLocalPaths
                        Log.d(TAG, "✅ 전체 다운로드 완료! 총 ${allLocalPaths.size}곡")
                    }
                )

                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "다운로드 실패"
                    _isDownloading.value = false
                    Log.e(TAG, "다운로드 실패", result.exceptionOrNull())
                }

            } catch (e: Exception) {
                _errorMessage.value = "오류 발생: ${e.message}"
                _isDownloading.value = false
                Log.e(TAG, "예외 발생", e)
            }
        }
    }

    // ========================================
    // 로컬 파일 재생
    // ========================================

    fun playLocalFile(filePath: String) {
        musicController.playLocalFile(filePath)
    }

    fun playLocalPlaylist(localPaths: List<String>, startIndex: Int = 0) {
        musicController.playLocalPlaylist(localPaths, startIndex = startIndex)
    }

    // ========================================
    // 서버 URL 재생 (suspend 함수 처리)
    // ========================================

    /**
     * ✅ playTrack - viewModelScope.launch로 감싸기
     */
    fun playTrack(music: Music) {
        viewModelScope.launch {
            try {
                // musicController.playMusic이 suspend 함수라면
                musicController.playMusic(music)
            } catch (e: Exception) {
                Log.e(TAG, "재생 오류", e)
                _errorMessage.value = "재생 실패: ${e.message}"
            }
        }
    }

    /**
     * ✅ playPlaylist - viewModelScope.launch로 감싸기
     */
    fun playPlaylist(musicList: List<Music>, startIndex: Int = 0) {
        viewModelScope.launch {
            try {
                // musicController.setPlaylist이 suspend 함수라면
                musicController.setPlaylist(musicList, startIndex)
            } catch (e: Exception) {
                Log.e(TAG, "플레이리스트 재생 오류", e)
                _errorMessage.value = "재생 실패: ${e.message}"
            }
        }
    }

    // ========================================
    // 기존 플레이리스트 로드
    // ========================================

    fun loadPlaylist() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                musicRepository.getPlaylist().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _playlist.value = result.data ?: emptyList()
                            _isLoading.value = false
                        }
                        is NetworkResult.Error -> {
                            _errorMessage.value = result.message ?: "플레이리스트를 불러올 수 없습니다"
                            _isLoading.value = false
                        }
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류가 발생했습니다: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setPlaylist(playlist: List<Music>) {
        _playlist.value = playlist
    }

    // ========================================
    // 재생 제어 (일반 함수)
    // ========================================

    fun togglePlayPause() {
        if (isPlaying.value) {
            musicController.pause()
        } else {
            musicController.play()
        }
    }

    fun play() {
        musicController.play()
    }

    fun pause() {
        musicController.pause()
    }

    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    fun seekForward() {
        musicController.seekForward()
    }

    fun seekBackward() {
        musicController.seekBackward()
    }

    fun skipToNext() {
        musicController.skipToNext()
    }

    fun skipToPrevious() {
        musicController.skipToPrevious()
    }

    // ========================================
    // 캐시 관리
    // ========================================

    fun getCacheSize(): String {
        return musicDownloadManager.getCacheSizeFormatted()
    }

    fun clearCache(): Int {
        return musicDownloadManager.clearAllCache()
    }

    // ========================================
    // 리소스 정리
    // ========================================

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}

/**
 * 재생 시간 포맷
 */
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}