package com.example.book_m_front.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book_m_front.network.ApiClient
import com.example.book_m_front.network.ApiService // Retrofit 서비스 import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EbookViewModel : ViewModel() {

    private val _bookContent = MutableStateFlow<String>("")
    val bookContent: StateFlow<String> = _bookContent

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val apiService: ApiService = ApiClient.getService() // API 서비스 인스턴스

    /**
     * ISBN을 사용하여 백엔드에서 책 본문을 String 형태로 가져옵니다.
     */
    fun fetchBookContent(isbn: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // API 호출: 백엔드 API의 실제 응답 형식에 맞춰야 합니다.
                // 예시: Response<String> 또는 Response<BookContentDto>
                val response = apiService.getBookContent(isbn) // ApiService에 getBookContent(isbn) 추가 필요

                if (response.isSuccessful && response.body() != null) {
                    _bookContent.value = response.body()!! // String 본문을 StateFlow에 할당
                } else {
                    _errorMessage.value = "콘텐츠를 불러오는데 실패했습니다. (코드: ${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
