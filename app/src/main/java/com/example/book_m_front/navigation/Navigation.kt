package com.example.book_m_front.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.book_m_front.ui.theme.ui.BookInfo
import com.example.book_m_front.ui.theme.ui.EbookViewerWithMusicScreen
import com.example.book_m_front.ui.theme.ui.MainDisplayScreen
import com.example.book_m_front.ui.theme.ui.MusicPlayerScreen
import com.example.book_m_front.ui.theme.ui.SearchedBookList
import com.example.book_m_front.ui.theme.ui.UserProfileScreen
import com.example.book_m_front.ui.theme.ui.login.LoginScreen
import com.example.book_m_front.ui.theme.ui.login.SignUpScreen
import com.example.book_m_front.ui.theme.ui.login.Start
import com.example.book_m_front.ui.theme.ui.login.StartLogin
import java.net.URLDecoder
import java.net.URLEncoder


// Navigation Routes 정의
sealed class Screen(val route: String) {
    object Start : Screen("start")
    object StartLogin : Screen("start_login")
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object MainDisplay : Screen("main_display")
    object User : Screen("user")

    object SearchedBookList : Screen("searched_book_list/{searchQuery}") {
        fun createRoute(searchQuery: String): String {
            return "searched_book_list/$searchQuery"
        }
    }

    object BookInfo : Screen("book_info/{bookIsbn}") {
        fun createRoute(bookIsbn: String): String {
            return "book_info/$bookIsbn"
        }
    }

    object EbookViewer : Screen("ebook_viewer/{bookTitle}/{bookAuthor}/{bookIsbn}") {
        fun createRoute(
            bookTitle: String,
            bookAuthor: String,
            bookIsbn: String,
            //bookFilePath: String
        ): String {
            // URL 인코딩을 위해 특수문자 처리
            val encodedTitle = URLEncoder.encode(bookTitle, "UTF-8")
            val encodedAuthor = URLEncoder.encode(bookAuthor, "UTF-8")
            //val encodedFilePath = URLEncoder.encode(bookFilePath, "UTF-8")
            return "ebook_viewer/$encodedTitle/$encodedAuthor/$bookIsbn"
        }
    }

    object MusicPlayer : Screen("music_player")
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        // ===== 시작 화면들 =====

        // 스플래시 화면
        composable(Screen.Start.route) {
            Start(
                onTimeout = {
                    navController.navigate(Screen.StartLogin.route) {
                        popUpTo(Screen.Start.route) { inclusive = true }
                    }
                }
            )
        }

        // 로그인/회원가입 선택 화면
        composable(Screen.StartLogin.route) {
            StartLogin(
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onSignInClick = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        // 로그인 화면
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Screen.MainDisplay.route) {
                        // 로그인 성공 시 이전 화면들을 모두 스택에서 제거
                        popUpTo(Screen.StartLogin.route) { inclusive = true }
                    }
                }
            )
        }

        // 회원가입 화면
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.MainDisplay.route) {
                        // 회원가입 성공 시 이전 화면들을 모두 스택에서 제거
                        popUpTo(Screen.StartLogin.route) { inclusive = true }
                    }
                }
            )
        }

        // ===== 메인 앱 화면들 =====

        // 메인 디스플레이 화면
        composable(Screen.MainDisplay.route) {
            MainDisplayScreen(
                onUserClick = {
                    navController.navigate(Screen.User.route)
                },
                onBookClick = { bookIsbn ->
                    navController.navigate(Screen.BookInfo.createRoute(bookIsbn))
                },
                onSearchButtonClick = { searchQuery ->
                    navController.navigate(Screen.SearchedBookList.createRoute(searchQuery))
                }
            )
        }

        // 사용자 프로필 화면
        composable(Screen.User.route) {
            UserProfileScreen(
                onNavigateToEbookViewer = { bookTitle, bookAuthor, bookIsbn, bookFilePath ->
                    navController.navigate(
                        Screen.EbookViewer.createRoute(
                            bookTitle = bookTitle,
                            bookAuthor = bookAuthor,
                            bookIsbn = bookIsbn,
                            //bookFilePath = bookFilePath
                        )
                    )
                }
            )
        }

        // 책 검색 결과 화면
        composable(
            route = Screen.SearchedBookList.route,
            arguments = listOf(
                navArgument("searchQuery") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val searchQuery = backStackEntry.arguments?.getString("searchQuery") ?: ""

            SearchedBookList(
                searchQueryFromNav = searchQuery,
                onBookClick = { bookIsbn ->
                    navController.navigate(Screen.BookInfo.createRoute(bookIsbn))
                }
            )
        }

        // 책 상세 정보 화면
        composable(
            route = Screen.BookInfo.route,
            arguments = listOf(
                navArgument("bookIsbn") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val bookIsbn = backStackEntry.arguments?.getString("bookIsbn") ?: ""

            BookInfo(
                bookIsbn = bookIsbn,
                // TODO: 필요한 다른 파라미터들 추가
                onBackClick = { navController.popBackStack() },
                onReadClick = { title, author, isbn ->
                    navController.navigate(
                        Screen.EbookViewer.createRoute(
                            bookTitle = title,
                            bookAuthor = author,
                            bookIsbn = isbn,
                            //bookFilePath = bookFilePath
                        )
                    )
                }

            )
        }

        // E-book 뷰어 화면 (음악 재생 기능 포함)
        composable(
            route = Screen.EbookViewer.route,
            arguments = listOf(
                navArgument("bookTitle") {
                    type = NavType.StringType
                },
                navArgument("bookAuthor") {
                    type = NavType.StringType
                },
                navArgument("bookIsbn") {
                    type = NavType.StringType
                },/*
                navArgument("bookFilePath") {
                    type = NavType.StringType
                }*/
            )
        ) { backStackEntry ->
            val bookTitle = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookTitle") ?: "",
                "UTF-8"
            )
            val bookAuthor = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookAuthor") ?: "",
                "UTF-8"
            )
            val bookIsbn = backStackEntry.arguments?.getString("bookIsbn") ?: ""
            /*val bookFilePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookFilePath") ?: "",
                "UTF-8"
            )*/

            EbookViewerWithMusicScreen(
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                bookIsbn = bookIsbn,
                //bookFilePath = bookFilePath,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        // 음악 플레이어 화면 (독립 화면으로 사용할 경우)
        composable(Screen.MusicPlayer.route) {
            MusicPlayerScreen()
        }
    }
}