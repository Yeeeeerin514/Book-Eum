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
                val navController = rememberNavController()

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
                        val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: ""
                        val bookAuthor = backStackEntry.arguments?.getString("bookAuthor") ?: ""
                        val bookIsbn = backStackEntry.arguments?.getString("bookIsbn") ?: ""
                        val bookFilePath = backStackEntry.arguments?.getString("bookFilePath") ?: ""

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
                }
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
}