package com.example.book_m_front.ui.theme.musicplayer

import com.example.book_m_front.network.dto.Music
import kotlinx.coroutines.flow.Flow

/**
 * =======================================================
 * 3. Repository Interface - 데이터 소스를 정의하는 설계도
 * =======================================================
 * 음악 데이터(플레이리스트 등)를 가져오는 기능의 규칙을 정의합니다.
 * 이렇게 인터페이스를 사용하면, 나중에 테스트하거나 다른 데이터 소스(예: 로컬 DB)로
 * 쉽게 교체할 수 있습니다.
 */
interface MusicRepository {
    // 플레이리스트를 가져오는 함수
    // Flow를 사용하여 데이터의 흐름(로딩, 성공, 실패)을 비동기적으로 처리합니다.
    suspend fun getPlaylist(): Flow<NetworkResult<List<Music>>>
}


/**
 * 네트워크 요청의 상태(성공, 실패, 로딩)를 효과적으로 관리하기 위한 Sealed Class.
 * 제네릭 타입 <T>를 사용하여 어떤 종류의 데이터든 담을 수 있습니다. (예: List<Music>)
 */
sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * API 호출이 성공했을 때의 상태.
     * @param data 성공적으로 받아온 데이터. (null이 아님)
     */
    class Success<T>(data: T) : NetworkResult<T>(data)

    /**
     * API 호출이 실패했을 때의 상태. (서버 오류, 네트워크 단절 등)
     * @param message 에러 메시지.
     * @param data 데이터가 있을 수도 있고 없을 수도 있음.
     */
    class Error<T>(message: String, data: T? = null) : NetworkResult<T>(data, message)

    /**
     * API 호출을 시작하고 응답을 기다리는 중인 상태.
     */
    class Loading<T> : NetworkResult<T>()
}