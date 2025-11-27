package com.example.book_m_front.network.ServerRequestAndResponse

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class UserData(
    val id: String,
    val password: String,
    val name: String,
    val spotifyAccount: String
)


class TestViewModel : ViewModel() {

    fun sendUserData(userData: UserData) {
        viewModelScope.launch {
            // --- 여기에 실제 Retrofit API 호출 코드가 들어갑니다 ---
            try {
                // 예시: val response = yourApiService.registerUser(userData)
                // if (response.isSuccessful) {
                //     Log.d("TestViewModel", "API 호출 성공: ${response.body()}")
                // } else {
                //     Log.e("TestViewModel", "API 호출 실패: ${response.errorBody()?.string()}")
                // }

                // 지금은 로그로 입력된 값을 확인합니다.
                println("API로 전송될 데이터: $userData")

            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                println("API 호출 중 오류 발생: ${e.message}")
            }
        }
    }
}