package com.example.book_m_front.network

import com.example.book_m_front.network.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

// 서버 기본 URL (실제 서버 주소로 변경 필요)
private const val BASE_URL = "http://localhost:8080/"
// 개발 서버
// private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"  // Android Emulator용

// Retrofit 빌더
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

/**
 * API 서비스 인터페이스
 * 모든 백엔드 통신은 이 인터페이스를 통해 이루어집니다
 */
interface ApiService {

    // ================== 인증 관련 ==================

    /**
     * 로그인
     * @param request 아이디, 비밀번호
     * @return 사용자 정보 및 액세스 토큰
     */
    @POST("users/login")
    suspend fun login(
        @Body request: UserLoginRequest
    ): Response<AuthResponse>

    /**
     * 회원가입
     * @param request 아이디, 비밀번호, 이름
     * @return 사용자 정보 및 액세스 토큰
     */
    @POST("users/signup")
    suspend fun signup(
        @Body request: UserJoinRequest
    ): Response<AuthResponse>

    /**
     * 토큰 갱신
     * @param request Refresh Token
     * @return 새로운 Access Token
     */
    @POST("users/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    /**
     * 로그아웃
     */
    @POST("users/logout")
    suspend fun logout(): Response<BaseResponse>


    // ================== 사용자 프로필 관련 ==================

    /**
     * 내 프로필 정보 조회
     * @return 사용자 프로필 정보
     */
    @GET("users/me")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    /**
     * 프로필 수정
     * @param request 수정할 정보 (이름, 이메일 등)
     * @return 수정된 프로필 정보
     */
    @PUT("users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserProfileResponse>

    /**
     * 프로필 이미지 업로드
     * @param image 이미지 파일
     * @return 업로드된 이미지 URL
     */
    @Multipart
    @POST("users/me/profile-image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>


    // ================== 책 검색 및 정보 ==================

    /**
     * 책 제목으로 검색
     * @param query 검색어
     * @param limit 한 페이지당 결과 수
     * @param offset 건너뛸 결과 수
     * @param sort 정렬 방식 (sim: 정확도, rank: 인기순, date: 최신순)
     * @return 검색 결과
     */
    @GET("books/search")
    suspend fun searchBookByTitle(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String = "sim"
    ): Response<SearchBookByTitleResponse>

    /**
     * 장르로 책 검색
     * @param genre 장르명
     * @param limit 한 페이지당 결과 수
     * @param offset 건너뛸 결과 수
     * @return 검색 결과
     */
    @GET("books/search/genre")
    suspend fun searchBookByGenre(
        @Query("genre") genre: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<SearchBookByTitleResponse>

    /**
     * ISBN으로 책 상세 정보 조회
     * @param isbn 책 ISBN
     * @return 책 상세 정보
     */
    @GET("books/{isbn}")
    suspend fun getBookInfo(
        @Path("isbn") isbn: String
    ): Response<BookInfoResponse>

    /**
     * 책 다운로드 URL 가져오기
     * @param isbn 책 ISBN
     * @return 다운로드 가능한 URL
     */
    @GET("books/{isbn}/download-url")
    suspend fun getBookDownloadUrl(
        @Path("isbn") isbn: String
    ): Response<BookDownloadResponse>

    /**
     * 책 파일 직접 다운로드 (파일이 작은 경우)
     * @param isbn 책 ISBN
     * @return 책 파일 스트림
     */
    @GET("books/{isbn}/content")
    @Streaming
    suspend fun downloadBookFile(
        @Path("isbn") isbn: String
    ): Response<ResponseBody>


    // ================== 책 업로드 및 관리 ==================

    /**
     * 새 책 등록 (EPUB 파일 업로드)
     * @param isbn ISBN
     * @param title 제목
     * @param author 저자
     * @param plot 줄거리
     * @param file EPUB 파일
     * @return 등록 결과
     */
    @Multipart
    @POST("books/register")
    suspend fun uploadBook(
        @Part("isbn") isbn: RequestBody,
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("plot") plot: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<BookUploadResponse>

    /**
     * 내가 업로드한 책 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 책 목록
     */
    @GET("users/me/uploaded-books")
    suspend fun getMyUploadedBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<BookListResponse>


    // ================== 플레이리스트 관련 ==================

    /**
     * 책의 AI 생성 플레이리스트 가져오기
     * @param isbn 책 ISBN
     * @return 전체 플레이리스트
     */
    @GET("books/{isbn}/playlist")
    suspend fun getPlaylist(
        @Path("isbn") isbn: String
    ): Response<PlaylistResponse>

    /**
     * 특정 챕터의 플레이리스트 가져오기 (챕터별 음악 제공 시)
     * @param isbn 책 ISBN
     * @param chapterNum 챕터 번호
     * @return 해당 챕터의 플레이리스트
     */
    @GET("books/{isbn}/chapters/{chapterNum}/playlist")
    suspend fun getPlaylistByChapter(
        @Path("isbn") isbn: String,
        @Path("chapterNum") chapterNum: Int
    ): Response<PlaylistResponse>

    /**
     * AI 플레이리스트 생성 요청 (아직 생성되지 않은 경우)
     * @param isbn 책 ISBN
     * @return 생성 요청 결과
     */
    @POST("books/{isbn}/generate-playlist")
    suspend fun generatePlaylist(
        @Path("isbn") isbn: String
    ): Response<PlaylistGenerateResponse>

    /**
     * 내가 좋아요한 플레이리스트 목록
     * @return 플레이리스트 목록
     */
    @GET("users/me/liked-playlists")
    suspend fun getLikedPlaylists(): Response<PlaylistListResponse>

    /**
     * 플레이리스트 좋아요
     * @param playlistId 플레이리스트 ID
     */
    @POST("playlists/{playlistId}/like")
    suspend fun likePlaylist(
        @Path("playlistId") playlistId: String
    ): Response<LikeResponse>

    /**
     * 플레이리스트 좋아요 취소
     * @param playlistId 플레이리스트 ID
     */
    @DELETE("playlists/{playlistId}/like")
    suspend fun unlikePlaylist(
        @Path("playlistId") playlistId: String
    ): Response<LikeResponse>


    // ================== 책 좋아요 및 저장 ==================

    /**
     * 내가 좋아요한 책 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 책 목록
     */
    @GET("users/me/liked-books")
    suspend fun getLikedBooks(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<BookListResponse>

    /**
     * 책 좋아요
     * @param isbn 책 ISBN
     * @return 좋아요 결과
     */
    @POST("books/{isbn}/like")
    suspend fun likeBook(
        @Path("isbn") isbn: String
    ): Response<LikeResponse>

    /**
     * 책 좋아요 취소
     * @param isbn 책 ISBN
     * @return 좋아요 취소 결과
     */
    @DELETE("books/{isbn}/like")
    suspend fun unlikeBook(
        @Path("isbn") isbn: String
    ): Response<LikeResponse>

    /**
     * 책이 좋아요 상태인지 확인
     * @param isbn 책 ISBN
     * @return 좋아요 여부
     */
    @GET("books/{isbn}/like-status")
    suspend fun checkLikeStatus(
        @Path("isbn") isbn: String
    ): Response<LikeStatusResponse>


    // ================== 읽기 히스토리 및 진행률 ==================

    /**
     * 읽기 히스토리 조회
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 읽기 히스토리
     */
    @GET("users/me/reading-history")
    suspend fun getReadingHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ReadingHistoryResponse>

    /**
     * 특정 책의 읽기 진행률 조회
     * @param isbn 책 ISBN
     * @return 진행률 정보
     */
    @GET("books/{isbn}/progress")
    suspend fun getReadingProgress(
        @Path("isbn") isbn: String
    ): Response<ReadingProgressResponse>

    /**
     * 읽기 진행률 저장
     * @param isbn 책 ISBN
     * @param request 현재 챕터, 진행률
     * @return 저장 결과
     */
    @POST("books/{isbn}/progress")
    suspend fun saveReadingProgress(
        @Path("isbn") isbn: String,
        @Body request: SaveProgressRequest
    ): Response<ReadingProgressResponse>


    // ================== 추천 및 인기 책 ==================

    /**
     * 오늘의 추천 책
     * @param limit 결과 수
     * @return 추천 책 목록
     */
    @GET("books/recommendations/today")
    suspend fun getTodayRecommendations(
        @Query("limit") limit: Int = 10
    ): Response<BookListResponse>

    /**
     * 인기 책 목록
     * @param limit 결과 수
     * @param period 기간 (daily, weekly, monthly)
     * @return 인기 책 목록
     */
    @GET("books/popular")
    suspend fun getPopularBooks(
        @Query("limit") limit: Int = 10,
        @Query("period") period: String = "weekly"
    ): Response<BookListResponse>

    /**
     * 최신 책 목록
     * @param limit 결과 수
     * @return 최신 책 목록
     */
    @GET("books/recent")
    suspend fun getRecentBooks(
        @Query("limit") limit: Int = 10
    ): Response<BookListResponse>

    /**
     * 맞춤 추천 책 (AI 기반)
     * @param limit 결과 수
     * @return 맞춤 추천 책 목록
     */
    @GET("books/recommendations/personalized")
    suspend fun getPersonalizedRecommendations(
        @Query("limit") limit: Int = 10
    ): Response<BookListResponse>

    /**
     * 비슷한 책 추천
     * @param isbn 기준 책 ISBN
     * @param limit 결과 수
     * @return 비슷한 책 목록
     */
    @GET("books/{isbn}/similar")
    suspend fun getSimilarBooks(
        @Path("isbn") isbn: String,
        @Query("limit") limit: Int = 10
    ): Response<BookListResponse>


    // ================== 리뷰 및 평점 (선택적) ==================

    /**
     * 책의 리뷰 목록 조회
     * @param isbn 책 ISBN
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 리뷰 목록
     */
    @GET("books/{isbn}/reviews")
    suspend fun getBookReviews(
        @Path("isbn") isbn: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ReviewListResponse>

    /**
     * 리뷰 작성
     * @param isbn 책 ISBN
     * @param request 리뷰 내용, 평점
     * @return 작성된 리뷰
     */
    @POST("books/{isbn}/reviews")
    suspend fun createReview(
        @Path("isbn") isbn: String,
        @Body request: CreateReviewRequest
    ): Response<ReviewResponse>

    /**
     * 리뷰 수정
     * @param reviewId 리뷰 ID
     * @param request 수정할 내용
     * @return 수정된 리뷰
     */
    @PUT("reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: String,
        @Body request: UpdateReviewRequest
    ): Response<ReviewResponse>

    /**
     * 리뷰 삭제
     * @param reviewId 리뷰 ID
     * @return 삭제 결과
     */
    @DELETE("reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: String
    ): Response<BaseResponse>


    // ================== 북마크 관리 (선택적) ==================

    /**
     * 책의 북마크 목록
     * @param isbn 책 ISBN
     * @return 북마크 목록
     */
    @GET("books/{isbn}/bookmarks")
    suspend fun getBookmarks(
        @Path("isbn") isbn: String
    ): Response<BookmarkListResponse>

    /**
     * 북마크 추가
     * @param isbn 책 ISBN
     * @param request 챕터, 위치, 메모
     * @return 추가된 북마크
     */
    @POST("books/{isbn}/bookmarks")
    suspend fun createBookmark(
        @Path("isbn") isbn: String,
        @Body request: CreateBookmarkRequest
    ): Response<BookmarkResponse>

    /**
     * 북마크 삭제
     * @param bookmarkId 북마크 ID
     * @return 삭제 결과
     */
    @DELETE("bookmarks/{bookmarkId}")
    suspend fun deleteBookmark(
        @Path("bookmarkId") bookmarkId: String
    ): Response<BaseResponse>
}

/**
 * API 서비스 싱글톤 객체
 * 앱 전역에서 사용
 */
object Api {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}