package com.example.book_m_front.ui.theme.musicplayer

// ============================================
// 4. 데이터 모델들
// ============================================

// Track: 음악 한 곡의 정보를 담는 데이터 클래스 -> DTO 파일의 Music이 대신하도록 함.
/*data class Music(   //-> 나중에 백엔드 api 받으면 수정해야됨.
    val id: Int,              // 고유 ID
    val title: String,        // 곡 제목 (예: "Dynamite")
    val artist: String,       // 아티스트 (예: "BTS")
    val audioUrl: String,    // 서버 URL (예: "https://myserver.com/music/song.mp3")
    val albumArtUrl: String,     // 앨범 커버 이미지 URL
    val duration: Long        // 곡 길이 (밀리초)
)*/

// 플레이어 상태를 나타내는 sealed class
sealed class PlayerState {
    data object Idle : PlayerState()      // 대기중 (아무것도 안함)
    data object Buffering : PlayerState() // 버퍼링중 (로딩중)
    data object Ready : PlayerState()     // 재생 준비 완료
    data object Ended : PlayerState()     // 재생 종료
}