package com.example.book_m_front.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 인증 토큰을 자동으로 추가하는 Interceptor
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // SharedPreferences에서 토큰 가져오기
        val token = getAccessToken(context)

        // 토큰이 없으면 원래 요청 그대로 진행
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // 토큰을 헤더에 추가한 새 요청 생성
        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }

    companion object {
        private const val PREFS_NAME = "BookMusicPrefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        /**
         * Access Token 저장
         */
        fun saveAccessToken(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        }

        /**
         * Access Token 가져오기
         */
        fun getAccessToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_ACCESS_TOKEN, null)
        }

        /**
         * Refresh Token 저장
         */
        fun saveRefreshToken(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        }

        /**
         * Refresh Token 가져오기
         */
        fun getRefreshToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_REFRESH_TOKEN, null)
        }

        /**
         * 토큰 삭제 (로그아웃)
         */
        fun clearTokens(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply()
        }
    }
}

/**
 * 개선된 Retrofit 빌더 (Interceptor 포함)
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    /**
     * Retrofit 인스턴스 생성
     * @param context Context (토큰 가져오기용)
     * @return Retrofit 인스턴스
     */
    fun create(context: Context): Retrofit {
        // 로깅 Interceptor (개발용)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // OkHttpClient 설정
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))  // 인증 토큰 자동 추가
            .addInterceptor(loggingInterceptor)  // 로깅
            .connectTimeout(30, TimeUnit.SECONDS)  // 연결 타임아웃
            .readTimeout(30, TimeUnit.SECONDS)  // 읽기 타임아웃
            .writeTimeout(30, TimeUnit.SECONDS)  // 쓰기 타임아웃
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

/**
 * API 서비스 싱글톤 (개선된 버전)
 */
object ApiClient {
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    /**
     * API 서비스 초기화
     * Application의 onCreate()에서 호출
     */
    fun initialize(context: Context) {
        if (retrofit == null) {
            retrofit = RetrofitClient.create(context)
            apiService = retrofit?.create(ApiService::class.java)
        }
    }

    /**
     * API 서비스 가져오기
     */
    fun getService(): ApiService {
        return apiService ?: throw IllegalStateException(
            "ApiClient가 초기화되지 않았습니다. Application의 onCreate()에서 initialize()를 호출하세요."
        )
    }

    /**
     * 로그인 성공 시 토큰 저장
     */
    fun saveTokens(context: Context, accessToken: String, refreshToken: String?) {
        AuthInterceptor.saveAccessToken(context, accessToken)
        refreshToken?.let { AuthInterceptor.saveRefreshToken(context, it) }
    }

    /**
     * 로그아웃
     */
    fun logout(context: Context) {
        AuthInterceptor.clearTokens(context)
    }
}

/**
 * BuildConfig가 없는 경우를 위한 임시 객체
 */
object BuildConfig {
    const val DEBUG = true
}