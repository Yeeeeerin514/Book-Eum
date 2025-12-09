package com.example.book_m_front

import android.app.Application
import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.repository.Repository

/**
 * Application 클래스
 * 앱이 시작될 때 한 번만 실행됩니다
 */
class BookMApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // API 클라이언트 초기화 (Retrofit + Interceptor)
        ApiClient.initialize(this)
        Repository.initialize(this)

        // 기타 초기화 작업들...
    }
}