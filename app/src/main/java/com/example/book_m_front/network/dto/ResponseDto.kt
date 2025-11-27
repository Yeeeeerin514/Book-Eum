package com.example.book_m_front.network.dto

//백엔드 서버에서 받는 데이터들.
//JSON으로 받아서, data class형식으로 바뀌어진다.

//로그인, 회원가입 시 response
data class AuthUser(
    val id : String,
    val name : String
)
data class AuthResponse(
    val user: AuthUser,
    val accessToken : String
)
//TODO : 유저 서랍 화면 진입
//TODO : 내 정보 수정 화면 진입
//TODO : 나의 책 추가 화면 (팝업)
//TODO : 신규도서 등록 시 받는 response DTO
//TODO : 메인 화면 진입

//책 제목으로 검색
data class Book(
    val isbn : String,
    val title : String,
    val author : String,
    val coverImg : String,
    val plot : String,
    val genre : ArrayList<String>   //arraylist는 받은 데이터를 수정할 필요가 있을 때 쓰는 거고, list는 수정할 필요가 없을 때 쓰는 거라 함.
)
data class SearchedBooks(
    val totalCount : Integer,
    val limit : Integer,
    val offset : Integer,
    val books : ArrayList<Book>
)
data class SearchBookByTitleResponse(   //<-이게 ApiService에서 실제로 사용되는 DTO
    val data : SearchedBooks
)

//TODO : 책 정보 화면 진입
//TODO : 책 정보 화면에서, 내 서재에 저장 버튼 클릭
//TODO : 이북 뷰어 화면 진입
//TODO : 플리 화면 진입
//TODO : 플리 화면에서, 내 서재에 저장 버튼 클릭


//------------------------------- 이 아래는 예전에 적은거라서 나중엔 사용 안 할 수도.
//Book 가져올 때 씀.
//TODO : 이거 백엔드에서 주는 것 맞춰서 수정 필요함.
data class BookItem(
    //val success: Boolean, //서버가 줄 수 있ㄴ는 잘 실행됏는지에 대한 값. 근데 뭐 없어도..
    //val message: String?,
    val isbn: String,
    val title: String,
    val author: String,
    val epubFileUrl: String, // 서버가 보내주는 클라우드 다운로드 URL
    //val isAnalyzed: Boolean,

    // @Transient: JSON 파싱 과정에서 이 필드를 완전히 무시함
    // 기본값은 반드시 필요함
    //@Transient val isDownloaded: Boolean = false

)

//TODO: 예린언니 백엔드에서 주는 것들 정리 끝나면 그에 맞춰 적기
//Playlist
data class Playlist(
    //val isbn: String,   //엥 이건 필요 없잖 여러곡에 대한거라 오히려 불가능.
    val playlist: List<Music>  //근데 아직 플리까진 안 하기로 해서, 아마 지금은 안쓸듯?
    //val playlist: MusicTrack //원래는 위 처럼 리스트로 받아야 함. 이 변수는 이번 시연만을 위한 것.
)

//Music (Playlist에 포함됨. 실제로 받는 건 PlaylistResponse.)
data class Music(  //백엔드가 주는 정보들로 다시 설정해야 함.
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String? = null,
    val audioUrl: String
)




