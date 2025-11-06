package com.example.book_m_front.network.ServerRequestAndResponse.dto

//백엔드 서버에서 받는 데이터들.
//JSON으로 받아서, data class형식으로 바뀌어진다.

//Book 가져올 때 씀.
//TODO : 이거 백엔드에서 주는 것 맞춰서 수정 필요함.
data class BookItem(
    //val success: Boolean, //서버가 줄 수 있ㄴ는 잘 실행됏는지에 대한 값. 근데 뭐 없어도..
    //val message: String?,
    val isbn: String,
    val title: String,
    val author: String,
    val epubFileUrl: String, // 서버가 보내주는 클라우드 다운로드 URL
    val isAnalyzed: Boolean,

    // @Transient: JSON 파싱 과정에서 이 필드를 완전히 무시함
    // 기본값은 반드시 필요함
    //@Transient val isDownloaded: Boolean = false

)

//Playlist
data class PlaylistResponse(
    //val isbn: String,   //엥 이건 필요 없잖 여러곡에 대한거라 오히려 불가능.
    val playlist: List<MusicTrack>  //근데 아직 플리까진 안 하기로 해서, 아마 지금은 안쓸듯?
    //val playlist: MusicTrack //원래는 위 처럼 리스트로 받아야 함. 이 변수는 이번 시연만을 위한 것.
)

//Music (Playlist에 포함됨. 실제로 받는 건 PlaylistResponse.)
data class MusicTrack(  //백엔드가 주는 정보들로 다시 설정해야 함.
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String? = null,
    val audioUrl: String
)