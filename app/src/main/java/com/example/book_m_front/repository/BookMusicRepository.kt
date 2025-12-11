package com.example.book_m_front.repository

import android.content.Context
import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * 프로덕션용 API Repository
 */
class BookMusicRepository(private val context: Context) {

    // 공통 헬퍼 함수
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다"
                    401 -> "로그인이 필요합니다"
                    403 -> "권한이 없습니다"
                    404 -> "요청한 정보를 찾을 수 없습니다"
                    500 -> "서버 오류가 발생했습니다"
                    else -> "오류가 발생했습니다 (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("인터넷 연결을 확인해주세요"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("서버 응답 시간이 초과되었습니다"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "알 수 없는 오류가 발생했습니다"))
        }
    }

    // 로그인
    suspend fun login(userId: String, password: String): Result<AuthResponse> {
        return safeApiCall {
            ApiClient.getService().login(
                UserLoginRequest(id = userId, password = password)
            )
        }.also { result ->
            result.onSuccess { auth ->
                ApiClient.saveTokens(context, auth.accessToken, auth.refreshToken)
            }
        }
    }

    // 회원가입
    suspend fun signup(
        userId: String,
        password: String,
        name: String,
        email: String? = null,
        phoneNumber: String? = null
    ): Result<AuthResponse> {
        return safeApiCall {
            ApiClient.getService().signup(
                UserJoinRequest(
                    id = userId,
                    password = password,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber
                )
            )
        }.also { result ->
            result.onSuccess { auth ->
                ApiClient.saveTokens(context, auth.accessToken, auth.refreshToken)
            }
        }
    }

    // 로그아웃
    suspend fun logout() {
        try {
            ApiClient.getService().logout()
        } catch (e: Exception) {
            // 에러 무시
        } finally {
            ApiClient.logout(context)
        }
    }

    // 사용자 프로필
    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return safeApiCall {
            ApiClient.getService().getUserProfile()
        }
    }

    // 프로필 수정
    suspend fun updateProfile(
        name: String? = null,
        email: String? = null
    ): Result<UserProfileResponse> {
        return safeApiCall {
            ApiClient.getService().updateProfile(
                UpdateProfileRequest(name = name, email = email)
            )
        }
    }

    // 책 검색
    suspend fun searchBooks(
        query: String,
        size: Int = 20,
        offset: Int = 0,
        sort: String = "sim"
    ): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().searchBookByTitle(
                query = query,
                size = size,
                offset = offset,
                sort = sort
            )
        }.map { it.content } //책 목록만 추출해서 보냄.
    }

    // 장르로 검색
    suspend fun searchBooksByGenre(
        genre: String,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().searchBookByGenre(
                genre = genre,
                limit = limit,
                offset = offset
            )
        }.map { it.content }
    }



    // 책 다운로드 URL
    suspend fun getBookDownloadUrl(isbn: String): Result<String> {
        return safeApiCall {
            ApiClient.getService().getBookDownloadUrl(isbn)
        }.map { it.downloadUrl }
    }

    // 플레이리스트
    suspend fun getPlaylist(isbn: String): Result<List<Music>> {
        return safeApiCall {
            ApiClient.getService().getPlaylist(isbn)
        }.map { it.playlist }
    }

    // 챕터별 플레이리스트
    suspend fun getPlaylistByChapter(
        isbn: String,
        chapterNum: Int
    ): Result<List<Music>> {
        return safeApiCall {
            ApiClient.getService().getPlaylistByChapter(isbn, chapterNum)
        }.map { it.playlist }
    }

    // 책 좋아요
    suspend fun likeBook(isbn: String): Result<Boolean> {
        return safeApiCall {
            ApiClient.getService().likeBook(isbn)
        }.map { it.isLiked }
    }

    // 책 좋아요 취소
    suspend fun unlikeBook(isbn: String): Result<Boolean> {
        return safeApiCall {
            ApiClient.getService().unlikeBook(isbn)
        }.map { it.isLiked }
    }

    // 좋아요 상태 확인
    suspend fun checkLikeStatus(isbn: String): Result<Boolean> {
        return safeApiCall {
            ApiClient.getService().checkLikeStatus(isbn)
        }.map { it.isLiked }
    }

    // 좋아요한 책 목록
    suspend fun getLikedBooks(page: Int = 0, size: Int = 20): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getLikedBooks(page, size)
        }.map { it.books }
    }

    // 읽기 히스토리
    suspend fun getReadingHistory(
        page: Int = 0,
        size: Int = 20
    ): Result<List<ReadingHistoryItem>> {
        return safeApiCall {
            ApiClient.getService().getReadingHistory(page, size)
        }.map { it.history }
    }

    // 읽기 진행률 조회
    suspend fun getReadingProgress(isbn: String): Result<ReadingProgressResponse> {
        return safeApiCall {
            ApiClient.getService().getReadingProgress(isbn)
        }
    }

    // 읽기 진행률 저장
    suspend fun saveReadingProgress(
        isbn: String,
        currentChapter: Int,
        progress: Int
    ): Result<Unit> {
        return safeApiCall {
            ApiClient.getService().saveReadingProgress(
                isbn = isbn,
                request = SaveProgressRequest(
                    currentChapter = currentChapter,
                    progress = progress
                )
            )
        }.map { }
    }

    // 오늘의 추천
    suspend fun getTodayRecommendations(limit: Int = 10): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getTodayRecommendations(limit)
        }.map { it.books }
    }

    // 인기 책
    suspend fun getPopularBooks(
        limit: Int = 10,
        period: String = "weekly"
    ): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getPopularBooks(limit, period)
        }.map { it.books }
    }

    // 최신 책
    suspend fun getRecentBooks(limit: Int = 10): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getRecentBooks(limit)
        }.map { it.books }
    }

    // 맞춤 추천
    suspend fun getPersonalizedRecommendations(limit: Int = 10): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getPersonalizedRecommendations(limit)
        }.map { it.books }
    }

    // 비슷한 책
    suspend fun getSimilarBooks(isbn: String, limit: Int = 10): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getSimilarBooks(isbn, limit)
        }.map { it.books }
    }

    // 내가 업로드한 책
    suspend fun getMyUploadedBooks(
        page: Int = 0,
        size: Int = 20
    ): Result<List<BookItem>> {
        return safeApiCall {
            ApiClient.getService().getMyUploadedBooks(page, size)
        }.map { it.books }
    }

    // 좋아요한 플레이리스트
    suspend fun getLikedPlaylists(): Result<List<PlaylistItem>> {
        return safeApiCall {
            ApiClient.getService().getLikedPlaylists()
        }.map { it.playlists }
    }
}

/**
 * Repository 싱글톤
 */
object Repository {
    private var instance: BookMusicRepository? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = BookMusicRepository(context.applicationContext)
        }
    }

    fun get(): BookMusicRepository {
        return instance ?: throw IllegalStateException(
            "Repository가 초기화되지 않았습니다"
        )
    }
}