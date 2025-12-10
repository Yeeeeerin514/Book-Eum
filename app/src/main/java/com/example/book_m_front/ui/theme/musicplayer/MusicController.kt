package com.example.book_m_front.ui.theme.musicplayer

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.book_m_front.network.dto.Music
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


// ============================================
// 3. MusicController.kt - 플레이어 제어 클래스
// ============================================
// 이 클래스는 실제로 음악을 재생/정지/스킵하는 모든 로직을 담당합니다.
// ViewModel이나 UI에서 이 클래스의 함수를 호출하면 됩니다.

class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // MediaController: 백그라운드 서비스의 플레이어를 제어하는 컨트롤러
    private var mediaController: MediaController? = null

    // StateFlow: Compose에서 관찰 가능한 상태 (값이 변경되면 UI 자동 업데이트)

    // 플레이어 상태 (대기중/버퍼링중/준비완료/종료)
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // 현재 재생 위치 (밀리초 단위, 예: 30000 = 30초)
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // 전체 음악 길이 (밀리초 단위)
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // 현재 재생 중인지 여부
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 현재 재생 중인 곡 정보
    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> = _currentMusic.asStateFlow()

    init {
        initializeController()
    }

    // MediaController 초기화 - 백그라운드 서비스와 연결
    fun initializeController() {    //private->public으로 변경. MusicPlayerModule에서 사용하기 위함.
        // 이미 초기화되었다면 다시 하지 않음
        if (mediaController != null) return

        // 서비스 토큰 생성 (어떤 서비스에 연결할지 지정)
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )

        // 비동기로 컨트롤러 생성 (서비스 연결까지 시간이 걸림)
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            // 연결 완료되면 컨트롤러 저장 및 리스너 설정
            mediaController = controllerFuture.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    // 플레이어 상태 변경을 감지하는 리스너 설정
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            // 재생 상태가 변경될 때 (대기중 → 버퍼링 → 재생 등)
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> _playerState.value = PlayerState.Idle
                    Player.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
                    Player.STATE_READY -> _playerState.value = PlayerState.Ready
                    Player.STATE_ENDED -> _playerState.value = PlayerState.Ended
                }
            }

            // 재생/일시정지 상태가 변경될 때
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate() // 재생 중이면 진행 상태 업데이트 시작
                }
            }

            // 다음 곡으로 넘어갈 때
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _duration.value = mediaController?.duration ?: 0L
            }
        })
    }

    // 0.1초마다 현재 재생 위치 업데이트
    // 이걸 해야 진행 바가 부드럽게 움직입니다
    private fun startProgressUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            while (mediaController?.isPlaying == true) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                delay(100) // 100ms = 0.1초마다 업데이트
            }
        }
    }

    // ========================================
    // 공개 함수들 - ViewModel이나 UI에서 호출하는 함수들
    // ========================================

    // 음악 한 곡 재생
    // 사용 예: musicController.playMusic(myTrack)
    fun playMusic(music: Music) {
        // MediaItem: ExoPlayer가 이해하는 음악 데이터 형식
        val mediaItem = MediaItem.Builder()
            .setUri(music.audioUrl) // 서버 URL 설정 (가장 중요!)
            .setMediaId(music.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(music.title)        // 곡 제목
                    .setArtist(music.artist)      // 아티스트
                    .setArtworkUri(music.albumArtUrl?.toUri()) // 앨범 커버 URL
                    .build()
            )
            .build()

        // 플레이어에 음악 설정하고 재생
        mediaController?.apply {
            setMediaItem(mediaItem) // 음악 설정
            prepare()                // 재생 준비 (버퍼링 시작)
            play()                   // 재생 시작
        }

        _currentMusic.value = music
    }

    // 재생 목록 설정 및 재생
    // 사용 예: musicController.setPlaylist(trackList, startIndex = 2)
    //         → 3번째 곡부터 재생 시작
    fun setPlaylist(music: List<Music>, startIndex: Int = 0) {
        // 여러 곡을 MediaItem 리스트로 변환
        val mediaItems = music.map { track ->
            MediaItem.Builder()
                .setUri(track.audioUrl)
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setArtworkUri(track.albumArtUrl?.toUri())
                        .build()
                )
                .build()
        }

        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0) // 목록 설정
            prepare()
            play()
        }

        if (music.isNotEmpty()) {
            _currentMusic.value = music[startIndex]
        }
    }

    // 재생
    fun play() {
        mediaController?.play()
    }

    // 일시정지
    fun pause() {
        mediaController?.pause()
    }

    // 특정 위치로 이동
    // 사용 예: seekTo(30000) → 30초 위치로 이동
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    // 15초 앞으로
    fun seekForward() {
        mediaController?.seekForward()
    }

    // 15초 뒤로
    fun seekBackward() {
        mediaController?.seekBack()
    }

    // 다음 곡
    fun skipToNext() {
        mediaController?.seekToNext()
    }

    // 이전 곡
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    // 플레이어 종료 (메모리 해제)
    fun release() {
        mediaController?.release()
        mediaController = null
    }
}
