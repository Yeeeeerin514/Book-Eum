package com.example.book_m_front.network.dto

//로그인
data class UserLoginRequest(
    val id : String,
    val password : String
)
//회원가입
data class UserJoinRequest(
    val id : String,
    val password : String,
    val name : String
)

//메인 화면의 사용자 정보 조회는 보통 현재 로그인한 사용자의 정보를 조회하므로,
// 서버에 별도의 요청 $DTO$ 없이 GET 요청을 보냅니다. (서버는 토큰을 통해 사용자를 식별합니다.)

//유저 서랍 화면 진입 -> 보내야 할 정보가 없으므로 DTO 없음. (그냥 accestoken이면 충분)
//내 정보 수정 화면 진입 -> 보내야 할 정보가 없으므로 DTO 없음.
//나의 책 추가 화면 (팝업) -> 파일과 함께 보내야 하므로 ApiService에서 직접 전달. DTO 따로 만들 수 없음.
//메인 화면 진입 -> 보내야 할 정보가 없으므로 DTO 없음.
//TODO : 책 정보 화면 진입
data class BookInformationRequest(

)
//TODO : 책 정보 화면에서, 내 서재에 저장 버튼 클릭
//TODO : 이북 뷰어 화면 진입
//TODO : 플리 화면 진입
//TODO : 플리 화면에서, 내 서재에 저장 버튼 클릭

//책 제목으로 검색
data class SearchBookByTitleRequest(
    val query : String,     //검색하려는 도서 제목
    val limit : Integer,    //한 번에 표시할 결과 수
    val offset : Integer,   //전체 결과에서 건너 뛸 개수
    val sort : String       //검색 결과 정렬 기준   - sim : 정확도 순  - rank : 인기 순
)

//책 열람