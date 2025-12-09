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
import com.example.book_m_front.ui.theme.ui.MusicPlayerScreen
import com.example.book_m_front.ui.theme.ui.UserProfileScreen
import com.example.book_m_front.ui.theme.ui_resource.Book_M_FrontTheme
import androidx.navigation.compose.composable
import com.example.book_m_front.navigation.AppNavigation
import com.example.book_m_front.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ✨ 2. 이 어노테이션을 클래스 위에 추가합니다.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Book_M_FrontTheme {
                AppNavigation()  // 이것만 호출하면 됨!
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
