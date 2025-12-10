package com.example.book_m_front.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.musicplayer.MusicController
import com.example.book_m_front.ui.theme.musicplayer.MusicRepository
import com.example.book_m_front.ui.theme.musicplayer.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * 🎵 음악 재생을 관리하는 통합 ViewModel
 *
 * 이 ViewModel은:
 * - MusicController를 통해 실제 음악 재생을 제어합니다
 * - MusicRepository를 통해 서버에서 플레이리스트를 가져옵니다
 * - UI에 필요한 상태(재생중/일시정지, 진행률 등)를 제공합니다
 */
@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository
) : ViewModel() {

    // ========================================
    // MusicController의 상태들을 UI에 노출
    // ========================================

    // 플레이어 상태 (Idle, Buffering, Ready, Ended)
    val playerState = musicController.playerState

    // 현재 재생 위치 (밀리초)
    val currentPosition = musicController.currentPosition

    // 총 재생 시간 (밀리초)
    val duration = musicController.duration

    // 재생 중인지 여부
    val isPlaying = musicController.isPlaying

    // 현재 재생 중인 트랙
    val currentTrack = musicController.currentMusic

    // ========================================
    // 플레이리스트 관리
    // ========================================

    // 현재 플레이리스트
    private val _playlist = MutableStateFlow<List<Music>>(emptyList())
    val playlist: StateFlow<List<Music>> = _playlist.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // ViewModel이 생성될 때 자동으로 플레이리스트 로드 (선택적)
        // loadPlaylist()
    }

    // ========================================
    // 서버에서 플레이리스트 가져오기
    // ========================================

    /**
     * 서버에서 플레이리스트를 가져옵니다
     */
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

    // ========================================
    // 테스트용: 직접 플레이리스트 설정
    // ========================================

    /**
     * 플레이리스트를 직접 설정합니다 (테스트용)
     */
    fun setPlaylist(playlist: List<Music>) {
        _playlist.value = playlist
    }

    // ========================================
    // UI에서 호출할 재생 제어 함수들
    // ========================================

    /**
     * 특정 곡을 재생합니다
     */
    fun playTrack(music: Music) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = musicController.playMusic(music)

                if (result.isFailure) {
                    _errorMessage.value = "재생 실패: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 플레이리스트를 설정하고 재생합니다
     * @param musicList 재생할 음악 리스트
     * @param startIndex 시작할 곡의 인덱스 (기본값: 0)
     */
    fun playPlaylist(musicList: List<Music>, startIndex: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = musicController.setPlaylist(musicList, startIndex)

                if (result.isFailure) {
                    _errorMessage.value = "플레이리스트 로드 실패: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "오류: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * 재생/일시정지를 토글합니다
     */
    fun togglePlayPause() {
        if (isPlaying.value) {
            musicController.pause()
        } else {
            musicController.play()
        }
    }

    /**
     * 재생을 시작합니다
     */
    fun play() {
        musicController.play()
    }

    /**
     * 일시정지합니다
     */
    fun pause() {
        musicController.pause()
    }

    /**
     * 특정 위치로 이동합니다
     * @param position 이동할 위치 (밀리초)
     */
    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    /**
     * 15초 앞으로 이동합니다
     */
    fun seekForward() {
        musicController.seekForward()
    }

    /**
     * 15초 뒤로 이동합니다
     */
    fun seekBackward() {
        musicController.seekBackward()
    }

    /**
     * 다음 곡으로 이동합니다
     */
    fun skipToNext() {
        musicController.skipToNext()
    }

    /**
     * 이전 곡으로 이동합니다
     */
    fun skipToPrevious() {
        musicController.skipToPrevious()
    }

    // ========================================
    // 리소스 정리
    // ========================================

    /**
     * ViewModel이 파괴될 때 플레이어 리소스를 정리합니다
     */
    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}

/**
 * 재생 시간을 포맷하는 헬퍼 함수
 * 예: 125초 -> "2:05"
 */
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}