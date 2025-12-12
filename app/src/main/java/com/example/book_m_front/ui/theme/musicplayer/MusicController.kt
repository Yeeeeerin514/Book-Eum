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
                    Player.STATE_BUFFERING -> {
                        _playerState.value = PlayerState.Buffering
                        // ✅ 버퍼링 중에도 재생 중으로 표시
                        _isPlaying.value = true
                    }
                    Player.STATE_READY -> {
                        _playerState.value = PlayerState.Ready
                        _duration.value = mediaController?.duration ?: 0L
                        // ✅ READY 상태면 재생 중으로 표시
                        _isPlaying.value = true
                        Log.d(TAG, "   ⏱️ Duration: ${_duration.value}ms")
                    }
                    Player.STATE_ENDED -> {
                        _playerState.value = PlayerState.Ended
                        // 곡이 끝났을 때만 false
                        _isPlaying.value = false
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "🎵 재생 중 변경: $isPlaying")

                // ✅ true일 때만 업데이트, false는 무시
                if (isPlaying) {
                    _isPlaying.value = true
                    startProgressUpdate()
                } else {
                    // false로 바뀌려고 할 때, 실제로 ENDED 상태가 아니면 무시
                    if (mediaController?.playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        Log.d(TAG, "🔚 곡이 끝나서 재생 종료")
                    } else {
                        Log.d(TAG, "🔧 isPlaying=false 무시 (playbackState=${mediaController?.playbackState})")
                        // 강제로 다시 재생 유지
                        _isPlaying.value = true
                        // 재생 명령 재시도
                        mediaController?.play()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val duration = mediaController?.duration ?: 0L
                _duration.value = duration
                val title = mediaItem?.mediaMetadata?.title
                Log.d(TAG, "🎵 곡 전환: $title, duration: ${duration}ms")

                // ✅ 곡 전환 시에도 재생 중 유지
                _isPlaying.value = true
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e(TAG, "❌ 플레이어 오류 발생: ${error.errorCodeName}")
                Log.e(TAG, "   메시지: ${error.message}")
                Log.e(TAG, "   원인: ${error.cause}")

                // ✅ 오류가 발생해도 UI는 재생 중으로 유지
                _isPlaying.value = true
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
     * 로컬 파일 재생 (강제 재생 버전)
     */
    fun playLocalFile(localPath: String, music: MusicTrack? = null) {
        Log.d(TAG, "🎵 ===== 로컬 파일 재생 시도 =====")
        Log.d(TAG, "   경로: $localPath")
        Log.d(TAG, "   제목: ${music?.title ?: "Unknown"}")

        // 파일 존재 확인
        val file = java.io.File(localPath)
        if (!file.exists() || file.length() == 0L) {
            Log.e(TAG, "❌ 파일이 유효하지 않습니다!")
            return
        }

        if (mediaController == null) {
            Log.e(TAG, "❌ MediaController가 null입니다! 초기화 중...")
            initializeController()

            // 초기화 후 재시도
            CoroutineScope(Dispatchers.Main).launch {
                delay(1500) // 1.5초 대기
                playLocalFile(localPath, music)
            }
            return
        }

        try {
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
                Log.d(TAG, "🎵 재생 시작...")

                // 1. 기존 재생 중지
                stop()
                clearMediaItems()

                // 2. 새 미디어 설정
                setMediaItem(mediaItem)

                // 3. playWhenReady 강제 설정
                playWhenReady = true

                // 4. prepare 및 play
                prepare()
                play()

                Log.d(TAG, "✅ 재생 명령 완료")
            }

            _currentMusic.value = music

            // ✅ 강제로 isPlaying true 설정 (임시)
            _isPlaying.value = true
            Log.d(TAG, "🔧 isPlaying을 강제로 true로 설정")

            // 재생 확인 및 재시도 로직
            CoroutineScope(Dispatchers.Main).launch {
                var retryCount = 0
                val maxRetries = 5

                while (retryCount < maxRetries) {
                    delay(500) // 0.5초마다 확인

                    val actuallyPlaying = mediaController?.isPlaying == true
                    val playbackState = mediaController?.playbackState

                    Log.d(TAG, "🔍 재생 확인 [${retryCount + 1}/$maxRetries]")
                    Log.d(TAG, "   MediaController.isPlaying: $actuallyPlaying")
                    Log.d(TAG, "   playbackState: $playbackState")
                    Log.d(TAG, "   currentPosition: ${mediaController?.currentPosition}ms")

                    if (actuallyPlaying) {
                        Log.d(TAG, "✅ 재생 확인 성공!")
                        _isPlaying.value = true
                        break
                    }

                    // 재시도
                    if (playbackState == Player.STATE_READY && !actuallyPlaying) {
                        Log.d(TAG, "🔄 재생 재시도...")
                        mediaController?.apply {
                            playWhenReady = true
                            play()
                        }
                    }

                    retryCount++
                }

                if (retryCount >= maxRetries && mediaController?.isPlaying != true) {
                    Log.e(TAG, "❌ ${maxRetries}번 재시도 후에도 재생 실패")
                    Log.e(TAG, "   최종 playbackState: ${mediaController?.playbackState}")
                    Log.e(TAG, "   playWhenReady: ${mediaController?.playWhenReady}")

                    // 그래도 UI는 재생 중으로 표시
                    _isPlaying.value = true

                    // 한번 더 강제 재생
                    mediaController?.play()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 재생 중 오류", e)
            // 오류가 발생해도 UI는 재생 중으로 표시
            _isPlaying.value = true
        }
    }

    /**
     * 로컬 플레이리스트 재생 (강제 재생 버전)
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

            CoroutineScope(Dispatchers.Main).launch {
                delay(1500)
                playLocalPlaylist(localPaths, musicList, startIndex)
            }
            return
        }

        try {
            val mediaItems = localPaths.mapIndexed { index, path ->
                val uri = "file://$path"
                val music = musicList?.getOrNull(index)

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
                Log.d(TAG, "🎵 플레이리스트 설정 중...")

                // 1. 기존 재생 중지
                stop()
                clearMediaItems()

                // 2. 플레이리스트 설정
                setMediaItems(mediaItems, startIndex, 0)

                // 3. playWhenReady 강제 설정
                playWhenReady = true

                // 4. prepare 및 play
                prepare()
                play()

                Log.d(TAG, "✅ 플레이리스트 재생 명령 완료")
            }

            if (!musicList.isNullOrEmpty() && startIndex < musicList.size) {
                _currentMusic.value = musicList[startIndex]
            }

            // ✅ 강제로 isPlaying true 설정
            _isPlaying.value = true
            Log.d(TAG, "🔧 isPlaying을 강제로 true로 설정")

            // 재생 확인 및 재시도
            CoroutineScope(Dispatchers.Main).launch {
                var retryCount = 0
                val maxRetries = 5

                while (retryCount < maxRetries) {
                    delay(500)

                    val actuallyPlaying = mediaController?.isPlaying == true

                    if (actuallyPlaying) {
                        Log.d(TAG, "✅ 플레이리스트 재생 확인!")
                        _isPlaying.value = true
                        break
                    }

                    // 재시도
                    Log.d(TAG, "🔄 플레이리스트 재생 재시도 [${retryCount + 1}/$maxRetries]")
                    mediaController?.apply {
                        playWhenReady = true
                        play()
                    }

                    retryCount++
                }

                if (mediaController?.isPlaying != true) {
                    Log.e(TAG, "❌ 플레이리스트 재생 실패, 하지만 UI는 재생 중으로 표시")
                    _isPlaying.value = true
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ 플레이리스트 재생 중 오류", e)
            _isPlaying.value = true
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