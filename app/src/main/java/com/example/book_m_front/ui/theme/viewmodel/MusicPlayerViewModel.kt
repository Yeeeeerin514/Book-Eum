package com.example.book_m_front.ui.theme.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.book_m_front.network.downloadMusicAndGetPath
import com.example.book_m_front.network.dto.Music
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 음악 재생을 관리하는 ViewModel
 * ExoPlayer를 사용하여 실제 음악 재생 기능 구현
 */
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    // ExoPlayer 인스턴스
    private var exoPlayer: ExoPlayer? = null

    // 현재 재생 상태
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // 플레이리스트
    private val _playlist = MutableStateFlow<List<Music>>(emptyList())
    val playlist: StateFlow<List<Music>> = _playlist.asStateFlow()

    // 현재 재생 중인 트랙 인덱스
    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    // 현재 트랙
    val currentTrack: Music?
        get() = _playlist.value.getOrNull(_currentTrackIndex.value)

    // 재생 진행 상태 (0.0 ~ 1.0)
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // 현재 재생 시간 (초)
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // 총 재생 시간 (초)
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()



    private val _isPreparing = MutableStateFlow(false)
    val isPreparing: StateFlow<Boolean> = _isPreparing


    /**
     * ExoPlayer 초기화
     */
    fun initializePlayer(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                // Player 이벤트 리스너 등록
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playerState.value = _playerState.value.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING
                        )
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.value = _playerState.value.copy(
                            isPlaying = isPlaying
                        )
                    }
                })

                // 재생 위치 업데이트를 위한 코루틴
                viewModelScope.launch {
                    while (true) {
                        exoPlayer?.let { player ->
                            val position = player.currentPosition / 1000 // 밀리초 -> 초
                            val totalDuration = player.duration / 1000

                            _currentPosition.value = position
                            _duration.value = if (totalDuration > 0) totalDuration else 0

                            if (totalDuration > 0) {
                                _progress.value = position.toFloat() / totalDuration.toFloat()
                            }
                        }
                        kotlinx.coroutines.delay(500) // 0.5초마다 업데이트
                    }
                }
            }
        }
    }

    /**
     * 플레이리스트 설정
     */
    // setPlaylist 함수를 아래와 같이 수정 또는 교체
    fun setPlaylist(playlist: List<Music>) { // DTO의 Music 클래스
        viewModelScope.launch {
            _isPreparing.value = true
            _playlist.value = playlist
            exoPlayer?.clearMediaItems()

            val context = getApplication<Application>().applicationContext
            val mediaItems = mutableListOf<MediaItem>()

            // 각 트랙에 대해 다운로드를 시도하고 MediaItem을 생성
            playlist.forEach { music ->
                // 1. 서버에서 음악 파일을 다운로드하고 로컬 경로를 얻어옴
                val localMusicPath = downloadMusicAndGetPath(context, music.id) // DTO에 musicId가 있어야 함

                if (localMusicPath != null) {
                    // 2. 다운로드 성공 시, 로컬 파일 경로를 Uri로 MediaItem 생성
                    val mediaItem = MediaItem.Builder()
                        .setUri(localMusicPath) // ✨ 로컬 파일 경로 사용
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(music.title)
                                .setArtist(music.artist)
                                .build()
                        )
                        .build()
                    mediaItems.add(mediaItem)
                } else {
                    println("음악 파일 준비 실패: ${music.title}")
                    // 실패 처리: 예) 플레이리스트에서 제외하거나, 에러 상태 표시
                }
            }

            exoPlayer?.addMediaItems(mediaItems)
            exoPlayer?.prepare()
            _isPreparing.value = false
        }
    }/*
    fun setPlaylist(musicList: List<Music>, startIndex: Int = 0) {
        _playlist.value = musicList
        _currentTrackIndex.value = startIndex

        // ExoPlayer에 미디어 아이템 추가
        exoPlayer?.let { player ->
            player.clearMediaItems()
            musicList.forEach { music ->
                val mediaItem = MediaItem.fromUri(music.audioUrl)
                player.addMediaItem(mediaItem)
            }
            player.seekTo(startIndex, 0)
            player.prepare()
            player.playWhenReady = true
        }
    }*/

    /**
     * 재생/일시정지 토글
     */
    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    /**
     * 재생
     */
    fun play() {
        exoPlayer?.play()
    }

    /**
     * 일시정지
     */
    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * 다음 곡
     */
    fun playNext() {
        exoPlayer?.let { player ->
            if (player.hasNextMediaItem()) {
                player.seekToNext()
                _currentTrackIndex.value = player.currentMediaItemIndex
            }
        }
    }

    /**
     * 이전 곡
     */
    fun playPrevious() {
        exoPlayer?.let { player ->
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious()
                _currentTrackIndex.value = player.currentMediaItemIndex
            }
        }
    }

    /**
     * 특정 트랙으로 이동
     */
    fun playTrackAt(index: Int) {
        if (index in _playlist.value.indices) {
            exoPlayer?.seekTo(index, 0)
            _currentTrackIndex.value = index
            exoPlayer?.play()
        }
    }

    /**
     * 특정 위치로 이동 (초 단위)
     */
    fun seekTo(positionSeconds: Long) {
        exoPlayer?.seekTo(positionSeconds * 1000)
    }

    /**
     * 진행률로 이동 (0.0 ~ 1.0)
     */
    fun seekToProgress(progress: Float) {
        exoPlayer?.let { player ->
            val position = (player.duration * progress).toLong()
            player.seekTo(position)
        }
    }

    /**
     * 반복 모드 설정
     */
    fun setRepeatMode(repeatMode: Int) {
        exoPlayer?.repeatMode = repeatMode
    }

    /**
     * 셔플 모드 설정
     */
    fun setShuffleModeEnabled(enabled: Boolean) {
        exoPlayer?.shuffleModeEnabled = enabled
    }

    /**
     * 리소스 해제
     */
    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}

/**
 * 플레이어 상태를 나타내는 데이터 클래스
 */
data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null
)

/**
 * 재생 시간을 포맷하는 헬퍼 함수
 * 예: 125초 -> "2:05"
 */
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}