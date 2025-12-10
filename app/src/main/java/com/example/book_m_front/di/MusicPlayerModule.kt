package com.example.book_m_front.di

import android.content.Context
import com.example.book_m_front.ui.theme.musicplayer.MusicController
import com.example.book_m_front.ui.theme.musicplayer.MusicRepository
import com.example.book_m_front.ui.theme.musicplayer.MusicRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🎵 음악 플레이어 관련 Hilt 의존성 주입 모듈
 *
 * 이 모듈은 음악 재생에 필요한 객체들을 제공합니다:
 * - MusicController: 실제 음악 재생을 담당
 * - MusicRepository: 서버에서 플레이리스트를 가져옴
 */
@Module
@InstallIn(SingletonComponent::class) // 앱 전체에서 사용 가능한 싱글톤
object MusicPlayerModule {

    /**
     * MusicController를 제공합니다
     *
     * @Singleton으로 표시되어 앱 전체에서 하나의 인스턴스만 사용됩니다.
     * 이렇게 하면 음악이 계속 재생되는 동안 상태가 유지됩니다.
     */
    @Provides
    @Singleton
    fun provideMusicController(
        @ApplicationContext context: Context
    ): MusicController {
        return MusicController(context).apply {
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