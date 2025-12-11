package com.example.book_m_front.ui.theme.musicplayer

import android.content.ComponentName
import android.content.Context
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
 * 🎵 음악 재생 컨트롤러 (중복 제거 버전)
 *
 * MusicDownloadService를 사용하여 음악 다운로드 및 재생 관리
 */
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDownloadService: MusicDownloadService
) {
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
        initializeController()
    }

    /**
     * MediaController 초기화 - 백그라운드 서비스와 연결
     */
    fun initializeController() {
        if (mediaController != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    /**
     * 플레이어 상태 변경 리스너 설정
     */
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> _playerState.value = PlayerState.Idle
                    Player.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
                    Player.STATE_READY -> _playerState.value = PlayerState.Ready
                    Player.STATE_ENDED -> _playerState.value = PlayerState.Ended
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _duration.value = mediaController?.duration ?: 0L
            }
        })
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
            // 1. 다운로드
            val downloadResult = musicDownloadService.downloadTrack(music)

            if (downloadResult.isFailure) {
                return Result.failure(downloadResult.exceptionOrNull()!!)
            }

            val localPath = downloadResult.getOrNull()!!

            // 2. 재생
            playLocalFile(localPath, music)

            _currentMusic.value = music
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 플레이리스트 재생 (서버에서 다운로드)
     */
    suspend fun setPlaylist(musicList: List<MusicTrack>, startIndex: Int = 0): Result<Unit> {
        return try {
            // 1. 모든 곡 다운로드
            val localPaths = mutableListOf<String>()

            for (music in musicList) {
                val downloadResult = musicDownloadService.downloadTrack(music)

                if (downloadResult.isSuccess) {
                    localPaths.add(downloadResult.getOrNull()!!)
                }
            }

            if (localPaths.isEmpty()) {
                return Result.failure(Exception("다운로드된 곡이 없습니다"))
            }

            // 2. 재생
            playLocalPlaylist(localPaths, musicList, startIndex)

            if (musicList.isNotEmpty() && startIndex < musicList.size) {
                _currentMusic.value = musicList[startIndex]
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로컬 파일 재생
     */
    fun playLocalFile(localPath: String, music: MusicTrack? = null) {
        val uri = "file://$localPath"

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
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        _currentMusic.value = music
    }

    /**
     * 로컬 플레이리스트 재생
     */
    fun playLocalPlaylist(
        localPaths: List<String>,
        musicList: List<MusicTrack>? = null,
        startIndex: Int = 0
    ) {
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
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }

        if (!musicList.isNullOrEmpty() && startIndex < musicList.size) {
            _currentMusic.value = musicList[startIndex]
        }
    }

    // ========================================
    // 🎮 재생 제어 함수들
    // ========================================

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun seekForward() {
        mediaController?.seekForward()
    }

    fun seekBackward() {
        mediaController?.seekBack()
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun release() {
        mediaController?.release()
        mediaController = null
    }
}