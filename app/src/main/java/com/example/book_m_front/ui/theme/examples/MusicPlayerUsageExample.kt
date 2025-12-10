package com.example.book_m_front.examples

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.network.dto.*
import kotlinx.coroutines.launch

/**
 * 실제 API 사용 예제들
 */

// ================== 1. 로그인 및 회원가입 ==================

/**
 * 로그인 예제
 */
suspend fun loginUser(
    context: Context,
    userId: String,
    password: String
): Result<AuthResponse> {
    return try {
        val response = ApiClient.getService().login(
            UserLoginRequest(
                id = userId,
                password = password
            )
        )

        if (response.isSuccessful && response.body() != null) {
            val authResponse = response.body()!!

            // 토큰 저장
            ApiClient.saveTokens(
                context,
                authResponse.accessToken,
                authResponse.refreshToken
            )

            Result.success(authResponse)
        } else {
            Result.failure(Exception("로그인 실패: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 회원가입 예제
 */
suspend fun signUpUser(
    context: Context,
    userId: String,
    password: String,
    name: String,
    email: String? = null
): Result<AuthResponse> {
    return try {
        val response = ApiClient.getService().signup(
            UserJoinRequest(
                id = userId,
                password = password,
                name = name,
                email = email
            )
        )

        if (response.isSuccessful && response.body() != null) {
            val authResponse = response.body()!!

            // 토큰 저장
            ApiClient.saveTokens(
                context,
                authResponse.accessToken,
                authResponse.refreshToken
            )

            Result.success(authResponse)
        } else {
            Result.failure(Exception("회원가입 실패"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 로그아웃 예제
 */
suspend fun logoutUser(context: Context) {
    try {
        ApiClient.getService().logout()
    } catch (e: Exception) {
        // 에러 무시 (로컬 토큰은 무조건 삭제)
    } finally {
        ApiClient.logout(context)
    }
}

// ================== 2. 책 검색 및 정보 ==================

/**
 * 책 검색 예제
 */
suspend fun searchBooks(
    query: String,
    limit: Int = 10,
    offset: Int = 0
): Result<List<BookItem>> {
    return try {
        val response = ApiClient.getService().searchBookByTitle(
            query = query,
            size = limit,
            offset = offset,
            sort = "sim"
        )

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.content)
        } else {
            Result.failure(Exception("검색 실패"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 책 상세 정보 가져오기
 */
suspend fun getBookDetails(isbn: String): Result<BookInfoResponse> {
    return try {
        val response = ApiClient.getService().getBookInfo(isbn)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("책 정보를 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ================== 3. 플레이리스트 ==================

/**
 * 플레이리스트 가져오기 (EbookViewer에서 사용)
 */
suspend fun fetchPlaylist(isbn: String): Result<List<Music>> {
    return try {
        val response = ApiClient.getService().getPlaylist(isbn)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.playlist)
        } else {
            Result.failure(Exception("플레이리스트를 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 챕터별 플레이리스트 가져오기
 */
suspend fun fetchPlaylistByChapter(
    isbn: String,
    chapterNum: Int
): Result<List<Music>> {
    return try {
        val response = ApiClient.getService().getPlaylistByChapter(isbn, chapterNum)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.playlist)
        } else {
            Result.failure(Exception("플레이리스트를 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ================== 4. 사용자 데이터 ==================

/**
 * 사용자 프로필 가져오기
 */
suspend fun getUserProfile(): Result<UserProfileResponse> {
    return try {
        val response = ApiClient.getService().getUserProfile()

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("프로필을 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 좋아요한 책 목록
 */
suspend fun getLikedBooks(page: Int = 0): Result<List<BookItem>> {
    return try {
        val response = ApiClient.getService().getLikedBooks(page = page)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.books)
        } else {
            Result.failure(Exception("목록을 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 읽기 히스토리
 */
suspend fun getReadingHistory(page: Int = 0): Result<List<ReadingHistoryItem>> {
    return try {
        val response = ApiClient.getService().getReadingHistory(page = page)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.history)
        } else {
            Result.failure(Exception("히스토리를 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ================== 5. 좋아요 및 진행률 ==================

/**
 * 책 좋아요
 */
suspend fun likeBook(isbn: String): Result<Boolean> {
    return try {
        val response = ApiClient.getService().likeBook(isbn)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.isLiked)
        } else {
            Result.failure(Exception("좋아요 실패"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 책 좋아요 취소
 */
suspend fun unlikeBook(isbn: String): Result<Boolean> {
    return try {
        val response = ApiClient.getService().unlikeBook(isbn)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.isLiked)
        } else {
            Result.failure(Exception("좋아요 취소 실패"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 읽기 진행률 저장
 */
suspend fun saveProgress(
    isbn: String,
    currentChapter: Int,
    progress: Int
): Result<Unit> {
    return try {
        val response = ApiClient.getService().saveReadingProgress(
            isbn = isbn,
            request = SaveProgressRequest(
                currentChapter = currentChapter,
                progress = progress
            )
        )

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("진행률 저장 실패"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ================== 6. 추천 책 ==================

/**
 * 오늘의 추천 책
 */
suspend fun getTodayRecommendations(): Result<List<BookItem>> {
    return try {
        val response = ApiClient.getService().getTodayRecommendations(limit = 10)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.books)
        } else {
            Result.failure(Exception("추천 책을 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 인기 책
 */
suspend fun getPopularBooks(): Result<List<BookItem>> {
    return try {
        val response = ApiClient.getService().getPopularBooks(
            limit = 10,
            period = "weekly"
        )

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.books)
        } else {
            Result.failure(Exception("인기 책을 불러올 수 없습니다"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// ================== 7. Composable에서 사용하는 방법 ==================

/**
 * Login 화면에서 실제 사용 예제
 */
@Composable
fun LoginExample() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun handleLogin() {
        isLoading = true
        scope.launch {
            val result = loginUser(context, userId, password)

            result.onSuccess { authResponse ->
                // 로그인 성공
                Toast.makeText(
                    context,
                    "${authResponse.user.name}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                // 메인 화면으로 이동
                // navController.navigate(Screen.MainDisplay.route)
            }

            result.onFailure { error ->
                // 로그인 실패
                Toast.makeText(
                    context,
                    "로그인 실패: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            isLoading = false
        }
    }

    // UI 구현...
}

/**
 * BookInfo 화면에서 실제 사용 예제
 */
@Composable
fun BookInfoExample(isbn: String) {
    val scope = rememberCoroutineScope()

    var bookInfo by remember { mutableStateOf<BookInfoResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isbn) {
        scope.launch {
            val result = getBookDetails(isbn)

            result.onSuccess { info ->
                bookInfo = info
            }

            result.onFailure { error ->
                // 에러 처리
            }

            isLoading = false
        }
    }

    // UI 구현...
}

/**
 * EbookViewer에서 플레이리스트 사용 예제
 */
@Composable
fun EbookViewerPlaylistExample(isbn: String) {
    val scope = rememberCoroutineScope()

    var playlist by remember { mutableStateOf<List<Music>>(emptyList()) }

    LaunchedEffect(isbn) {
        scope.launch {
            val result = fetchPlaylist(isbn)

            result.onSuccess { musicList ->
                playlist = musicList
                // MusicPlayerViewModel에 설정
                // musicPlayerViewModel.setPlaylist(musicList)
            }

            result.onFailure { error ->
                // 플레이리스트 로드 실패
            }
        }
    }

    // UI 구현...
}

// ================== 8. 에러 처리 헬퍼 ==================

/**
 * API 에러를 사용자 친화적 메시지로 변환
 */
fun getErrorMessage(error: Throwable): String {
    return when {
        error.message?.contains("Unable to resolve host") == true ->
            "인터넷 연결을 확인해주세요"
        error.message?.contains("timeout") == true ->
            "서버 응답이 없습니다. 잠시 후 다시 시도해주세요"
        error.message?.contains("401") == true ->
            "로그인이 필요합니다"
        error.message?.contains("403") == true ->
            "권한이 없습니다"
        error.message?.contains("404") == true ->
            "요청한 정보를 찾을 수 없습니다"
        error.message?.contains("500") == true ->
            "서버 오류가 발생했습니다"
        else ->
            error.message ?: "알 수 없는 오류가 발생했습니다"
    }
}