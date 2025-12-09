package com.example.book_m_front.ui.theme.musicplayer

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture


// ============================================
// 2. MusicService.kt - 백그라운드 음악 재생 서비스
// ============================================
// 왜 Service가 필요한가?
// - 앱을 최소화하거나 다른 앱으로 전환해도 음악이 계속 재생되게 하려면
// - 서비스(백그라운드 작업)로 만들어야 합니다.
// - 액티비티에서 직접 재생하면 앱 종료 시 음악도 멈춥니다.

//@HiltAndroidService
class MusicService : MediaSessionService() {
    // MediaSession: 음악 재생을 관리하고 외부(알림, 블루투스 등)와 통신
    private var mediaSession: MediaSession? = null

    // ExoPlayer: 실제 음악을 재생하는 플레이어 엔진
    private lateinit var player: ExoPlayer

    // 서비스가 처음 생성될 때 호출됨
    override fun onCreate() {
        super.onCreate()
        initializePlayer()  // 플레이어 초기화
        initializeSession() // 세션 초기화
    }

    // ExoPlayer 초기화
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC) // 음악 타입 설정
                    .setUsage(C.USAGE_MEDIA) // 미디어 용도
                    .build(),
                true // 오디오 포커스 자동 처리 (다른 앱 소리 자동 조절)
            )
            .setHandleAudioBecomingNoisy(true) // 이어폰 뽑으면 자동 일시정지
            .build()
    }

    // MediaSession 초기화
    // MediaSession은 플레이어를 외부에서 제어할 수 있게 해줌
    // (알림 버튼, 블루투스 이어폰, 잠금화면 위젯 등)
    private fun initializeSession() {
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()
    }

    // 외부에서 이 서비스의 MediaSession을 요청할 때 반환
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // 서비스가 종료될 때 정리 작업
    override fun onDestroy() {
        mediaSession?.run {
            player.release() // 플레이어 리소스 해제 (메모리 정리)
            release()         // 세션 해제
            mediaSession = null
        }
        super.onDestroy()
    }

    // MediaSession 콜백: 외부에서 음악 추가 요청 시 처리
    private inner class MediaSessionCallback : MediaSession.Callback {
        // 음악 아이템이 추가될 때 호출됨
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            // 각 음악 아이템에 실제 재생 URL을 설정
            val updatedMediaItems = mediaItems.map { mediaItem ->
                mediaItem.buildUpon()
                    .setUri(mediaItem.requestMetadata.mediaUri)
                    .build()
            }
            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}