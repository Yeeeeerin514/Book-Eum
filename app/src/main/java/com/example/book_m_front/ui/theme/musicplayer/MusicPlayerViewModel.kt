package com.example.book_m_front.ui.theme.musicplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================
// 5. ViewModel - UI와 비즈니스 로직 연결
// ============================================
// ViewModel은 화면이 회전되거나 재생성되어도 데이터를 유지합니다.
// UI는 ViewModel의 StateFlow를 관찰하여 자동으로 업데이트됩니다.

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository // 서버에서 음악 목록 가져오기
) : ViewModel() {

    // MusicController의 상태들을 그대로 노출
    // UI에서 이 값들을 collectAsState()로 관찰합니다
    val playerState = musicController.playerState
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration
    val isPlaying = musicController.isPlaying
    val currentTrack = musicController.currentTrack

    // 재생 목록
    private val _playlist = MutableStateFlow<List<Track>>(emptyList())
    val playlist: StateFlow<List<Track>> = _playlist.asStateFlow()

    init {
        loadPlaylist() // ViewModel 생성 시 자동으로 재생 목록 로드
    }

    // 서버에서 재생 목록 가져오기
    private fun loadPlaylist() {
        viewModelScope.launch {
            musicRepository.getPlaylist().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _playlist.value = result.data
                    }
                    is NetworkResult.Error -> {
                        // 에러 처리 (예: 토스트 메시지 표시)
                    }
                    else -> { /* 로딩 중 */ }
                }
            }
        }
    }

    // UI에서 호출할 함수들

    // 특정 곡 재생
    fun playTrack(track: Track) {
        musicController.playMusic(track)
    }

    // 재생 목록 전체 재생
    fun playPlaylist(tracks: List<Track>, startIndex: Int = 0) {
        musicController.setPlaylist(tracks, startIndex)
    }

    // 재생/일시정지 토글
    fun togglePlayPause() {
        if (isPlaying.value) {
            musicController.pause()
        } else {
            musicController.play()
        }
    }

    // 슬라이더 드래그 시 호출
    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    // 다음 곡 버튼
    fun skipToNext() {
        musicController.skipToNext()
    }

    // 이전 곡 버튼
    fun skipToPrevious() {
        musicController.skipToPrevious()
    }

    // ViewModel이 파괴될 때 플레이어 정리
    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}