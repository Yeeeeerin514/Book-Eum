package com.example.book_m_front.ui.theme.musicplayer

import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.network.ApiService
import com.example.book_m_front.network.dto.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 MusicRepository의 실제 구현체
 *
 * 서버로부터 음악 데이터를 가져오는 역할을 담당합니다.
 * Hilt를 통해 싱글톤으로 관리됩니다.
 */
@Singleton
class MusicRepositoryImpl @Inject constructor() : MusicRepository {

    // ApiService 인스턴스를 ApiClient로부터 가져옵니다
    private val apiService: ApiService = ApiClient.getService()

    /**
     * 서버로부터 음악 플레이리스트를 가져옵니다
     *
     * Flow를 사용하여 로딩 → 성공/실패 상태를 순차적으로 방출합니다.
     *
     * @return Flow<NetworkResult<List<Music>>> 플레이리스트 데이터 스트림
     */
    override suspend fun getPlaylist(): Flow<NetworkResult<List<Music>>> = flow {
        // 1. 로딩 상태를 먼저 방출
        emit(NetworkResult.Loading())

        try {
            // 2. 서버에 API 호출
            // TODO: 실제 ISBN을 파라미터로 받도록 수정 필요
            val testIsbn = "9788934942467" // 임시 테스트용 ISBN
            val response = apiService.getPlaylist(testIsbn)

            // 3. 응답 확인
            if (response.isSuccessful && response.body() != null) {
                val playlist = response.body()!!.playlist
                // 성공 상태와 데이터를 방출
                emit(NetworkResult.Success(playlist))
            } else {
                // 서버 에러 (400, 404, 500 등)
                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다"
                    404 -> "플레이리스트를 찾을 수 없습니다"
                    500 -> "서버 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                emit(NetworkResult.Error(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            // 네트워크 연결 오류
            emit(NetworkResult.Error("인터넷 연결을 확인해주세요"))
        } catch (e: java.net.SocketTimeoutException) {
            // 타임아웃
            emit(NetworkResult.Error("서버 응답 시간이 초과되었습니다"))
        } catch (e: Exception) {
            // 기타 예외
            emit(NetworkResult.Error("오류가 발생했습니다: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO) // IO 스레드에서 실행

    /**
     * 특정 ISBN의 플레이리스트를 가져옵니다 (확장 버전)
     *
     * @param isbn 책의 ISBN
     * @return Flow<NetworkResult<List<Music>>> 플레이리스트 데이터 스트림
     */
    suspend fun getPlaylistByIsbn(isbn: String): Flow<NetworkResult<List<Music>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getPlaylist(isbn)

            if (response.isSuccessful && response.body() != null) {
                val playlist = response.body()!!.playlist
                emit(NetworkResult.Success(playlist))
            } else {
                emit(NetworkResult.Error("플레이리스트를 불러오는데 실패했습니다 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("네트워크 오류: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 특정 챕터의 플레이리스트를 가져옵니다
     *
     * @param isbn 책의 ISBN
     * @param chapterNum 챕터 번호
     * @return Flow<NetworkResult<List<Music>>> 플레이리스트 데이터 스트림
     */
    suspend fun getPlaylistByChapter(
        isbn: String,
        chapterNum: Int
    ): Flow<NetworkResult<List<Music>>> = flow {
        emit(NetworkResult.Loading())

        try {
            val response = apiService.getPlaylistByChapter(isbn, chapterNum)

            if (response.isSuccessful && response.body() != null) {
                val playlist = response.body()!!.playlist
                emit(NetworkResult.Success(playlist))
            } else {
                emit(NetworkResult.Error("챕터 플레이리스트를 불러오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("네트워크 오류: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}