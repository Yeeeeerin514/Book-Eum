package com.example.book_m_front.network

import com.example.book_m_front.network.ServerRequestAndResponse.BookUploadResponse
import com.example.book_m_front.network.dto.AuthResponse
import com.example.book_m_front.network.dto.BookItem
import com.example.book_m_front.network.dto.Playlist
import com.example.book_m_front.network.dto.SearchBookByTitleRequest
import com.example.book_m_front.network.dto.SearchBookByTitleResponse
import com.example.book_m_front.network.dto.UserJoinRequest
import com.example.book_m_front.network.dto.UserLoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


//연결할 HTTP 추가
//private const val BASE_URL = "https://10.210.61.15:8080/api"
private const val BASE_URL = "https://localhost:8080/api"


//retrofit 빌더 객체 추가
private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())  //string응답용
    .addConverterFactory(GsonConverterFactory.create())     //JSON 응답용
    .baseUrl(BASE_URL)  //기본 url 추가
    .build()    //객체 빌드!


//retrofit이 HTTP를 요청해서 웹 서버와 통신하는 방법을 정의함.
//내가 백엔드 서버에 주는 것.
interface ApiService{
    @GET("photos")  //GET 요청임을 알림. 괄호안의 내용은 엔드포인트인데, 이것도 나중에 수정이 필요함.
    suspend fun getTitle() : String //웹 서비스에서 응답 문자열을 가져올거임. 이 함수를 호출하면, retrofit 객체가 base_url에 위에서 작성한 엔드포인트를 추가함.

    //로그인
    @POST("/users/login")
    suspend fun login(@Body request: UserLoginRequest): Response<AuthResponse>
    //회원가입
    @POST("/users/signup")
    suspend fun signup(@Body request: UserJoinRequest): Response<AuthResponse>

    //TODO : 유저 서랍 진입
    //TODO : 내 정보 화면 진입
    //TODO : 내 정보 화면 수정후 확인버튼

    //TODO:새로운 책 등록
    @Multipart
    @POST("/books/register")  // 실제 엔드포인트로 변경 필요 -> ok
    suspend fun uploadBook( //얘는 파일도 주는 거라서, dto를 사용하지 못함.
        @Part("isbn") isbn: RequestBody,
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("plot") plot: RequestBody,
        @Part file: MultipartBody.Part  //key(이름표)가 epubFile임.
    ): Response<BookUploadResponse>//TODO : 이거 response 수정하기

    //TODO : 메인 화면 진입

    //책 검색 화면에서 책 검색함.
    @GET("/books/search/title")
    suspend fun searchBookByTitle(@Body request: SearchBookByTitleRequest): Response<SearchBookByTitleResponse>

    //TODO : 책 정보화면 진입
    //TODO : 책 정보화면에서 책 저장 버튼 클릭
    //TODO : 이북뷰어 화면 진입
    //TODO : 플리 화면 진입
    //TODO : 플리 화면에서 플리 저장 버튼 클릭

    //------------------------이 아래는 예전 거. 새로 다 적으면 사용 안 할듯?

    //책 가져오기 (isbn으로)
    @GET("/books/{isbn}/content")   //실제 엔드포인트로 변경 필요
    suspend fun downloadBook(
        @Path("isbn") isbn: String
    ): BookItem  //Response<ResponseBody> 이거에서 바꿈.

    //음악 플리 가져오기
    //TODO: 예린언니 백엔드에서 주는 것들 정리 끝나면 그에 맞춰 수정
    @GET("/books/{isbn}/{chapterNum}/play-aiPlaylist ???")
    suspend fun getPlaylist(@Path("isbn") isbn: String): Playlist

    //TODO : 유저 정보 서버에서 받아오기


    @POST("/books/{isbn}/content")
    suspend fun uploadContent(
        @Part("id") id: String,
        @Part("password") password: String,
        @Part("name") name : String,
        @Part("spotifyAccount") spotifyAccount : String
    ): Response<BookUploadResponse>
}


object Api{ //싱글톤 객체 : 전역에서 접근가능. (보통은 권장되지 않지만, api할 땐 사용함) (한 객체만을 허용하는 객체임)
    val retrofitService : ApiService by lazy {  //지연 초기화 : 최초 사용 시 ㄱㅊ은 초기화를 위함..
        retrofit.create(ApiService::class.java)
    }
}

