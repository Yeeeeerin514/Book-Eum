package com.example.book_m_front.network.dto

// ================== 공통 응답 ==================

//기본 응답 : 성공/실패 를 알려줌.
data class BaseResponse(
    val success: Boolean,
    val message: String? = null
)

//에러 응답
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail
)

//에러 세부사항
data class ErrorDetail(
    val code: String,
    val message: String
)

// ================== 인증 관련 ==================

//로그인 요청
data class UserLoginRequest(
    val id: String,
    val password: String
)

//회원가입 요청
data class UserJoinRequest(
    val id: String,
    val password: String,
    val name: String,
    val email: String? = null
)

//토큰 갱신 요청
data class RefreshTokenRequest(
    val refreshToken: String
)

//로그인, 회원가입 응답
data class AuthUser(
    val id: String,
    val name: String,
    val email: String? = null
)

data class AuthResponse(
    val success: Boolean = true,
    val user: AuthUser,
    val accessToken: String,
    val refreshToken: String? = null
)

// ================== 사용자 프로필 ==================

/**
 * 사용자 프로필 응답
 */
data class UserProfileResponse(
    val id: String,
    val name: String = "Unknown",
    val email: String? = "12345@cau.ac.kr",
    val profileImageUrl: String? = null,
    val spotifyAccount: String? = "spotify@account",
    val createdAt: String? = null
)

/**
 * 프로필 수정 요청
 */
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val spotifyAccount: String? = null
)

/**
 * 이미지 업로드 응답
 */
data class ImageUploadResponse(
    val success: Boolean,
    val imageUrl: String
)

// ================== 책 정보 ==================

//책 기본 정보 (리스트용)
data class BookItem(
    val isbn: String,
    val title: String,
    val author: String,
    val coverImg: String? = null,
    val publisher: String? = null,
    val genre: List<String>? = null
)

//책 상세 정보
data class BookInfoResponse(
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String? = null,
    val plot: String? = null,
    val coverImg: String? = null,
    val keywords: List<String>? = null,
    val tableOfContents: String? = null,
    val genre: List<String>? = null,
    val publishDate: String? = null,
    val pageCount: Int? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val isLiked: Boolean = false,
    val hasPlaylist: Boolean = false
)

//책 검색 응답
data class SearchedBooks(
    val totalCount: Int,
    val limit: Int,
    val offset: Int,
    val books: List<BookItem>
)

data class SearchBookByTitleResponse(
    val success: Boolean = true,
    val data: SearchedBooks
)

//책 목록 응답
data class BookListResponse(
    val success: Boolean = true,
    val books: List<BookItem>,
    val totalCount: Int? = null,
    val page: Int? = null,
    val size: Int? = null
)

/**
 * 책 다운로드 URL 응답
 */
data class BookDownloadResponse(
    val success: Boolean = true,
    val isbn: String,
    val downloadUrl: String,        //TODO : 책을 텍스트 파일로 받아와야 함.
    val expiresAt: String? = null  // URL 만료 시간
)

/**
 * 책 업로드 응답
 */
data class BookUploadResponse(
    val success: Boolean,
    val message: String,
    val bookId: String? = null,
    val isbn: String? = null
)

// ================== 플레이리스트 ==================

/**
 * 음악 트랙 정보
 */
data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String? = null,
    val audioUrl: String? = null,
    val duration: Int? = null,  // 초 단위
    val chapterIndex: Int? = null  // 챕터별 플레이리스트인 경우
)

/**
 * 플레이리스트 응답
 */
data class PlaylistResponse(
    val success: Boolean = true,
    val isbn: String,
    val playlistId: String? = null,
    val playlistName: String? = null,
    val playlist: List<Music>,
    val generatedAt: String? = null
)

/**
 * 플레이리스트 생성 응답
 */
data class PlaylistGenerateResponse(
    val success: Boolean,
    val message: String,
    val playlistId: String? = null,
    val status: String  // "generating", "completed", "failed"
)

/**
 * 플레이리스트 아이템 (목록용)
 */
data class PlaylistItem(
    val playlistId: String,
    val title: String,
    val creator: String? = null,
    val coverImg: String? = null,
    val trackCount: Int? = null,
    val isLiked: Boolean = false
)

/**
 * 플레이리스트 목록 응답
 */
data class PlaylistListResponse(
    val success: Boolean = true,
    val playlists: List<PlaylistItem>
)

// ================== 좋아요 ==================

/**
 * 좋아요 응답
 */
data class LikeResponse(
    val success: Boolean,
    val isLiked: Boolean,
    val likeCount: Int? = null
)

/**
 * 좋아요 상태 응답
 */
data class LikeStatusResponse(
    val success: Boolean = true,
    val isLiked: Boolean
)

// ================== 읽기 히스토리 및 진행률 ==================

/**
 * 읽기 히스토리 항목
 */
data class ReadingHistoryItem(
    val book: BookItem,
    val lastReadChapter: Int,
    val progress: Int,  // 0-100
    val lastReadAt: String,
    val totalChapters: Int? = null
)

/**
 * 읽기 히스토리 응답
 */
data class ReadingHistoryResponse(
    val success: Boolean = true,
    val history: List<ReadingHistoryItem>,
    val totalCount: Int? = null
)

/**
 * 진행률 저장 요청
 */
data class SaveProgressRequest(
    val currentChapter: Int,
    val progress: Int,  // 0-100
    val lastPosition: String? = null  // 상세 위치 (예: "page 42, line 5")
)

/**
 * 진행률 응답
 */
data class ReadingProgressResponse(
    val success: Boolean = true,
    val isbn: String,
    val currentChapter: Int,
    val progress: Int,
    val lastReadAt: String? = null,
    val lastPosition: String? = null
)

// ================== 리뷰 및 평점 ==================

/**
 * 리뷰 정보
 */
data class Review(
    val reviewId: String,
    val userId: String,
    val userName: String,
    val userProfileImg: String? = null,
    val rating: Int,  // 1-5
    val content: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
)

/**
 * 리뷰 목록 응답
 */
data class ReviewListResponse(
    val success: Boolean = true,
    val reviews: List<Review>,
    val totalCount: Int,
    val averageRating: Double? = null
)

/**
 * 리뷰 작성 요청
 */
data class CreateReviewRequest(
    val rating: Int,  // 1-5
    val content: String
)

/**
 * 리뷰 수정 요청
 */
data class UpdateReviewRequest(
    val rating: Int? = null,
    val content: String? = null
)

/**
 * 리뷰 응답
 */
data class ReviewResponse(
    val success: Boolean = true,
    val review: Review
)

// ================== 북마크 ==================

/**
 * 북마크 정보
 */
data class Bookmark(
    val bookmarkId: String,
    val chapterIndex: Int,
    val chapterTitle: String? = null,
    val position: String? = null,
    val memo: String? = null,
    val createdAt: String
)

/**
 * 북마크 목록 응답
 */
data class BookmarkListResponse(
    val success: Boolean = true,
    val bookmarks: List<Bookmark>
)

/**
 * 북마크 생성 요청
 */
data class CreateBookmarkRequest(
    val chapterIndex: Int,
    val chapterTitle: String? = null,
    val position: String? = null,
    val memo: String? = null
)

/**
 * 북마크 응답
 */
data class BookmarkResponse(
    val success: Boolean = true,
    val bookmark: Bookmark
)

// ================== 통계 및 기타 ==================

/**
 * 읽기 통계
 */
data class ReadingStats(
    val totalBooksRead: Int,
    val currentlyReading: Int,
    val totalReadingTime: Int,  // 분 단위
    val favoriteGenres: List<String>
)

/**
 * 알림 설정
 */
data class NotificationSettings(
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
    val newBookAlert: Boolean,
    val readingReminder: Boolean
)