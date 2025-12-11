package com.example.book_m_front.di

import android.content.Context
import com.example.book_m_front.ui.theme.musicplayer.MusicController
import com.example.book_m_front.ui.theme.musicplayer.MusicDownloadManager
import com.example.book_m_front.ui.theme.musicplayer.MusicDownloader
import com.example.book_m_front.ui.theme.musicplayer.MusicRepository
import com.example.book_m_front.ui.theme.musicplayer.MusicRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🎵 음악 재생 관련 의존성 주입 모듈 (업데이트)
 *
 * 제공하는 싱글톤:
 * 1. MusicDownloadManager - 음악 파일 다운로드 및 캐싱 (새로 추가!)
 * 2. MusicController - 음악 재생 제어
 * 3. MusicRepository - 서버 API 통신
 */
@Module
@InstallIn(SingletonComponent::class) // 앱 전체에서 사용 가능한 싱글톤
object MusicPlayerModule {

    /**
     * ✅ 새로 추가: MusicDownloadManager 제공
     *
     * 음악 파일 다운로드 및 캐싱을 담당
     */
    @Provides
    @Singleton
    fun provideMusicDownloadManager(
        @ApplicationContext context: Context
    ): MusicDownloadManager {
        return MusicDownloadManager(context)
    }


    /**
     * MusicController를 제공합니다
     *
     * @Singleton으로 표시되어 앱 전체에서 하나의 인스턴스만 사용됩니다.
     * 이렇게 하면 음악이 계속 재생되는 동안 상태가 유지됩니다.
     */

    @Provides
    @Singleton
    fun provideMusicDownloader(
        @ApplicationContext context: Context
    ): MusicDownloader {
        return MusicDownloader(context)
    }
    @Provides
    @Singleton
    fun provideMusicController(
        @ApplicationContext context: Context,
        musicDownloader: MusicDownloader  // ✅ 추가
    ): MusicController {
        return MusicController(context, musicDownloader).apply {
            // Controller 생성 시 초기화
            initializeController()
        }
    }

    /**
     * MusicRepository의 구현체를 제공합니다
     *
     * 인터페이스(MusicRepository)를 반환하지만 실제로는
     * 구현체(MusicRepositoryImpl)를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideMusicRepository(
        repositoryImpl: MusicRepositoryImpl
    ): MusicRepository {
        return repositoryImpl
    }

}