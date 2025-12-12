package com.example.book_m_front.ui.theme.musicplayer

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.book_m_front.network.dto.MusicTrack
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

/**
 * 🎵 음악 재생 컨트롤러 (디버깅 버전)
 */
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDownloadService: MusicDownloadService
) {
    companion object {
        private const val TAG = "MusicController"
    }

    // MediaController: 백그라운드 서비스의 플레이어를 제어
    private var mediaController: MediaController? = null

    // StateFlow: UI에서 관찰 가능한 상태
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentMusic = MutableStateFlow<MusicTrack?>(null)
    val currentMusic: StateFlow<MusicTrack?> = _currentMusic.asStateFlow()

    init {
        Log.d(TAG, "🎵 MusicController 초기화 시작")
        initializeController()
    }

    /**
     * MediaController 초기화 - 백그라운드 서비스와 연결
     */
    fun initializeController() {
        if (mediaController != null) {
            Log.d(TAG, "⚠️ MediaController 이미 초기화됨")
            return
        }

        Log.d(TAG, "🔄 MediaController 초기화 시작...")

        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                Log.d(TAG, "✅ MediaController 초기화 완료!")
                Log.d(TAG, "   플레이어 상태: ${mediaController?.playbackState}")
                Log.d(TAG, "   재생 중: ${mediaController?.isPlaying}")
                setupPlayerListener()
            } catch (e: Exception) {
                Log.e(TAG, "❌ MediaController 초기화 실패", e)
            }
        }, MoreExecutors.directExecutor())
    }

    /**
     * 플레이어 상태 변경 리스너 설정
     */
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN($playbackState)"
                }
                Log.d(TAG, "🎵 재생 상태 변경: $stateName")

                when (playbackState) {
                    Player.STATE_IDLE -> _playerState.value = PlayerState.Idle
                    Player.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
                    Player.STATE_READY -> {
                        _playerState.value = PlayerState.Ready
                        _duration.value = mediaController?.duration ?: 0L
                        Log.d(TAG, "   ⏱️ Duration: ${_duration.value}ms")
                    }
                    Player.STATE_ENDED -> _playerState.value = PlayerState.Ended
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "🎵 재생 중 변경: $isPlaying")
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val duration = mediaController?.duration ?: 0L
                _duration.value = duration
                val title = mediaItem?.mediaMetadata?.title
                Log.d(TAG, "🎵 곡 전환: $title, duration: ${duration}ms")
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "❌ 플레이어 오류 발생: ${error.errorCodeName}")
                Log.e(TAG, "   메시지: ${error.message}")
                Log.e(TAG, "   원인: ${error.cause}")
            }
        })
        Log.d(TAG, "✅ 플레이어 리스너 설정 완료")
    }

    /**
     * 재생 진행 상태 업데이트 (0.1초마다)
     */
    private fun startProgressUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            while (mediaController?.isPlaying == true) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                delay(100)
            }
        }
    }

    // ========================================
    // 🎵 음악 재생 함수들
    // ========================================

    /**
     * 단일 음악 재생 (서버에서 다운로드)
     */
    suspend fun playMusic(music: MusicTrack): Result<Unit> {
        return try {
            Log.d(TAG, "🎵 음악 재생 요청: ${music.title}")

            // 1. 다운로드
            val downloadResult = musicDownloadService.downloadTrack(music)

            if (downloadResult.isFailure) {
                Log.e(TAG, "❌ 다운로드 실패: ${music.title}")
                return Result.failure(downloadResult.exceptionOrNull()!!)
            }

            val localPath = downloadResult.getOrNull()!!
            Log.d(TAG, "✅ 다운로드 완료: $localPath")

            // 2. 재생
            playLocalFile(localPath, music)

            _currentMusic.value = music
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 재생 중 오류", e)
            Result.failure(e)
        }
    }

    /**
     * 플레이리스트 재생 (서버에서 다운로드)
     */
    suspend fun setPlaylist(musicList: List<MusicTrack>, startIndex: Int = 0): Result<Unit> {
        return try {
            Log.d(TAG, "🎵 플레이리스트 재생 요청: ${musicList.size}곡")

            // 1. 모든 곡 다운로드
            val localPaths = mutableListOf<String>()

            for (music in musicList) {
                val downloadResult = musicDownloadService.downloadTrack(music)

                if (downloadResult.isSuccess) {
                    localPaths.add(downloadResult.getOrNull()!!)
                }
            }

            if (localPaths.isEmpty()) {
                Log.e(TAG, "❌ 다운로드된 곡이 없습니다")
                return Result.failure(Exception("다운로드된 곡이 없습니다"))
            }

            Log.d(TAG, "✅ ${localPaths.size}곡 다운로드 완료")

            // 2. 재생
            playLocalPlaylist(localPaths, musicList, startIndex)

            if (musicList.isNotEmpty() && startIndex < musicList.size) {
                _currentMusic.value = musicList[startIndex]
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 플레이리스트 재생 중 오류", e)
            Result.failure(e)
        }
    }

    /**
     * 로컬 파일 재생
     */
    fun playLocalFile(localPath: String, music: MusicTrack? = null) {
        Log.d(TAG, "🎵 ===== 로컬 파일 재생 시도 =====")
        Log.d(TAG, "   경로: $localPath")
        Log.d(TAG, "   제목: ${music?.title ?: "Unknown"}")
        Log.d(TAG, "   아티스트: ${music?.artist ?: "Unknown"}")

        // 파일 존재 확인
        val file = java.io.File(localPath)
        Log.d(TAG, "📂 파일 검증:")
        Log.d(TAG, "   존재: ${file.exists()}")
        Log.d(TAG, "   크기: ${file.length()} bytes")
        Log.d(TAG, "   읽기 가능: ${file.canRead()}")
        Log.d(TAG, "   절대 경로: ${file.absolutePath}")

        if (!file.exists()) {
            Log.e(TAG, "❌ 파일이 존재하지 않습니다!")
            return
        }

        if (file.length() == 0L) {
            Log.e(TAG, "❌ 파일 크기가 0입니다!")
            return
        }

        if (mediaController == null) {
            Log.e(TAG, "❌ MediaController가 null입니다! 초기화 재시도...")
            initializeController()

            // 초기화 후 재시도
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // 1초 대기
                if (mediaController != null) {
                    Log.d(TAG, "✅ MediaController 재초기화 성공, 재시도...")
                    playLocalFile(localPath, music)
                } else {
                    Log.e(TAG, "❌ MediaController 재초기화 실패")
                }
            }
            return
        }

        Log.d(TAG, "🎵 MediaController 상태:")
        Log.d(TAG, "   현재 재생 상태: ${mediaController?.playbackState}")
        Log.d(TAG, "   재생 중: ${mediaController?.isPlaying}")

        val uri = "file://$localPath"
        Log.d(TAG, "📂 파일 URI: $uri")

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(music?.id ?: localPath)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(music?.title ?: "Local Music")
                    .setArtist(music?.artist ?: "Unknown")
                    .setArtworkUri(music?.albumArtUrl?.toUri())
                    .build()
            )
            .build()

        mediaController?.apply {
            Log.d(TAG, "🎵 MediaItem 설정 중...")
            setMediaItem(mediaItem)
            Log.d(TAG, "🎵 prepare() 호출...")
            prepare()
            Log.d(TAG, "🎵 play() 호출...")
            play()
            Log.d(TAG, "✅ 재생 명령 완료")

            // 상태 확인
            Log.d(TAG, "🔍 즉시 상태 확인:")
            Log.d(TAG, "   playbackState: $playbackState")
            Log.d(TAG, "   isPlaying: $isPlaying")
            Log.d(TAG, "   playWhenReady: $playWhenReady")
        }

        _currentMusic.value = music

        // 1초 후 재생 상태 확인
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            Log.d(TAG, "🔍 ===== 1초 후 재생 상태 체크 =====")
            Log.d(TAG, "   MediaController isPlaying: ${mediaController?.isPlaying}")
            Log.d(TAG, "   MediaController playbackState: ${mediaController?.playbackState}")
            Log.d(TAG, "   MediaController currentPosition: ${mediaController?.currentPosition}ms")
            Log.d(TAG, "   MediaController duration: ${mediaController?.duration}ms")
            Log.d(TAG, "   StateFlow isPlaying: ${_isPlaying.value}")
            Log.d(TAG, "   StateFlow playerState: ${_playerState.value}")

            if (mediaController?.isPlaying == false) {
                Log.e(TAG, "❌ 1초 후에도 재생되지 않음!")
                Log.e(TAG, "   playWhenReady: ${mediaController?.playWhenReady}")
                Log.e(TAG, "   mediaItemCount: ${mediaController?.mediaItemCount}")
            }
        }
    }

    /**
     * 로컬 플레이리스트 재생
     */
    fun playLocalPlaylist(
        localPaths: List<String>,
        musicList: List<MusicTrack>? = null,
        startIndex: Int = 0
    ) {
        Log.d(TAG, "🎵 로컬 플레이리스트 재생 시도: ${localPaths.size}곡, startIndex=$startIndex")

        if (mediaController == null) {
            Log.e(TAG, "❌ MediaController가 null입니다!")
            initializeController()
            return
        }

        val mediaItems = localPaths.mapIndexed { index, path ->
            val uri = "file://$path"
            val music = musicList?.getOrNull(index)

            Log.d(TAG, "📂 [$index] URI: $uri")

            MediaItem.Builder()
                .setUri(uri)
                .setMediaId(music?.id ?: path)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(music?.title ?: "Track ${index + 1}")
                        .setArtist(music?.artist ?: "Unknown")
                        .setArtworkUri(music?.albumArtUrl?.toUri())
                        .build()
                )
                .build()
        }

        mediaController?.apply {
            Log.d(TAG, "🎵 ${mediaItems.size}개 MediaItem 설정 중...")
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
            Log.d(TAG, "✅ 플레이리스트 재생 시작 명령 완료")
        }

        if (!musicList.isNullOrEmpty() && startIndex < musicList.size) {
            _currentMusic.value = musicList[startIndex]
        }

        // 재생 상태 확인
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            Log.d(TAG, "🔍 플레이리스트 상태 체크 - isPlaying: ${mediaController?.isPlaying}, currentIndex: ${mediaController?.currentMediaItemIndex}")
        }
    }

    // ========================================
    // 🎮 재생 제어 함수들
    // ========================================

    fun play() {
        Log.d(TAG, "▶️ 재생 요청")
        mediaController?.play()
    }

    fun pause() {
        Log.d(TAG, "⏸️ 일시정지 요청")
        mediaController?.pause()
    }

    fun seekTo(position: Long) {
        Log.d(TAG, "⏩ Seek 요청: ${position}ms")
        mediaController?.seekTo(position)
    }

    fun seekForward() {
        Log.d(TAG, "⏩ 앞으로 이동")
        mediaController?.seekForward()
    }

    fun seekBackward() {
        Log.d(TAG, "⏪ 뒤로 이동")
        mediaController?.seekBack()
    }

    fun skipToNext() {
        Log.d(TAG, "⏭️ 다음 곡")
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        Log.d(TAG, "⏮️ 이전 곡")
        mediaController?.seekToPrevious()
    }

    fun release() {
        Log.d(TAG, "🛑 MediaController 해제")
        mediaController?.release()
        mediaController = null
    }
}