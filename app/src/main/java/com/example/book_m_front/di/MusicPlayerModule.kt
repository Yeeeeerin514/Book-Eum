package com.example.book_m_front.di

import android.content.Context
import com.example.book_m_front.ui.theme.musicplayer.MusicController
import com.example.book_m_front.ui.theme.musicplayer.MusicDownloadService
import com.example.book_m_front.ui.theme.musicplayer.MusicRepository
import com.example.book_m_front.ui.theme.musicplayer.MusicRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🎵 음악 재생 관련 의존성 주입 모듈 (중복 제거 버전)
 *
 * 제공하는 싱글톤:
 * 1. MusicDownloadService - 통합 음악 다운로드 서비스 (MusicDownloader + MusicDownloadManager 통합)
 * 2. MusicController - 음악 재생 제어
 * 3. MusicRepository - 서버 API 통신
 */
@Module
@InstallIn(SingletonComponent::class)
object MusicPlayerModule {

    /**
     * 🎵 통합 음악 다운로드 서비스 제공
     *
     * MusicDownloader와 MusicDownloadManager의 기능을 하나로 통합
     */
    @Provides
    @Singleton
    fun provideMusicDownloadService(
        @ApplicationContext context: Context
    ): MusicDownloadService {
        return MusicDownloadService(context)
    }

    /**
     * 🎮 음악 컨트롤러 제공
     *
     * @Singleton으로 표시되어 앱 전체에서 하나의 인스턴스만 사용
     * 음악이 계속 재생되는 동안 상태 유지
     */
    @Provides
    @Singleton
    fun provideMusicController(
        @ApplicationContext context: Context,
        musicDownloadService: MusicDownloadService
    ): MusicController {
        return MusicController(context, musicDownloadService).apply {
            initializeController()
        }
    }

    /**
     * 📦 MusicRepository 구현체 제공
     *
     * 인터페이스(MusicRepository)를 반환하지만
     * 실제로는 구현체(MusicRepositoryImpl)를 제공
     */
    @Provides
    @Singleton
    fun provideMusicRepository(
        repositoryImpl: MusicRepositoryImpl
    ): MusicRepository {
        return repositoryImpl
    }
}