package com.example.book_m_front

import android.app.Application
import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.repository.Repository
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 클래스
 * 앱이 시작될 때 한 번만 실행됩니다
 */
//hilt가 의존성 주입을 할 수 있도록 함.
@HiltAndroidApp
class BookMApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // API 클라이언트 초기화 (Retrofit + Interceptor)
        ApiClient.initialize(this)
        Repository.initialize(this)

        // 기타 초기화 작업들...
    }
}