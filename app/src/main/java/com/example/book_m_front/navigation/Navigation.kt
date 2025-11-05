package com.example.book_m_front.navigation

// 2. Navigation Routes 정의 (새 파일: Navigation.kt)
sealed class Screen(val route: String) {
    object UserProfile : Screen("user_profile")
    object EbookViewer : Screen("ebook_viewer/{bookTitle}/{bookAuthor}/{bookIsbn}/{bookFilePath}") {   //bookisbn -> bookFilepath으로바꿈
        fun createRoute(bookTitle: String, bookAuthor: String, bookIsbn: String, bookFilePath: String): String {
            return "ebook_viewer/$bookTitle/$bookAuthor/$bookIsbn/$bookFilePath"
        }
    }
    object Home : Screen("home")
    object Search : Screen("search")
    //여기에 계속 화면 경로들을 추가하나봄
}

