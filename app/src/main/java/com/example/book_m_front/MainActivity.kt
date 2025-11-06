package com.example.book_m_front

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.book_m_front.ui.theme.ui.EbookViewerScreen
import com.example.book_m_front.ui.theme.ui.MusicPlayerScreen
import com.example.book_m_front.ui.theme.ui.UserProfileScreen
import com.example.book_m_front.ui.theme.ui_resource.Book_M_FrontTheme
import androidx.navigation.compose.composable
import com.example.book_m_front.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Book_M_FrontTheme {
                //MusicPlayerScreen()
                //UserProfileScreen()
                /*EbookViewerScreen(
                    bookTitle = "책 제목",
                    bookAuthor = "김나린",
                    bookIsbn  = "1234456",
                    bookFilePath = "1234",
                    onBackClick = {}
                )*/

                /*val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.UserProfile.route
                ) {
                    // UserProfile 화면
                    composable(route = Screen.UserProfile.route) {
                        UserProfileScreen(
                            onNavigateToEbookViewer = { title, author, isbn, filepath ->
                                navController.navigate(
                                    Screen.EbookViewer.createRoute(title, author, isbn, filepath)
                                )
                            }
                        )
                    }

                    // EbookViewer 화면 (파라미터 받기)
                    composable(
                        route = Screen.EbookViewer.route,
                        arguments = listOf(
                            navArgument("bookTitle") { type = NavType.StringType },
                            navArgument("bookAuthor") { type = NavType.StringType },
                            navArgument("bookIsbn") { type = NavType.StringType },
                            navArgument("bookFilePath") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: "-"
                        val bookAuthor = backStackEntry.arguments?.getString("bookAuthor") ?: "-"
                        val bookIsbn = backStackEntry.arguments?.getString("bookIsbn") ?: "-"
                        val bookFilePath = backStackEntry.arguments?.getString("bookFilePath") ?: "content://com.android.providers.downloads.documents/document/msf%3A1000000033"

                        EbookViewerScreen(
                            bookTitle = bookTitle,
                            bookAuthor = bookAuthor,
                            bookIsbn = bookIsbn,
                            bookFilePath = bookFilePath,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                }*/

                // 2. 테스트하고 싶은 EbookViewerScreen을 직접 호출합니다.
                EbookViewerScreen(
                    bookTitle = "테스트용 책 제목",
                    bookAuthor = "테스트용 저자",
                    bookIsbn  = "9781234567890",
                    // 이 경로가 가장 중요합니다! 실제 존재하는 파일 경로를 넣어야 합니다.
                    bookFilePath = "content://com.android.providers.downloads.documents/document/msf%3A1000000033",
                    onBackClick = {
                        // 테스트 중에는 뒤로가기 버튼이 특별한 동작을 할 필요가 없습니다.
                        // 예를 들어, 간단한 로그를 남기거나 토스트 메시지를 띄울 수 있습니다.
                        println("뒤로가기 버튼 클릭됨")
                    }
                )
                /*setContent {
                    MaterialTheme {
                        MusicPlayerScreen()
                        //UserProfileScreen()
                        //EbookViewerScreen()
                    }
                }*/
            }
        }
    }
}

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Book_M_FrontTheme {
        Greeting("Android")
    }
}*/
