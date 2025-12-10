package com.example.book_m_front.ui.theme.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.network.dto.PlaylistItem
import com.example.book_m_front.ui.theme.ui.badge.Badge
import com.example.book_m_front.ui.theme.ui_resource.Book_M_FrontTheme

//데이터 패키지로 따로 관리하는 게 좋을 수도
/*data class PlaylistItem(//흠 뭐 id같은거 필요없나?
    val title: String,
    val creator: String
)*/
@Composable
fun PlaylistRow(playlists: List<PlaylistItem>, darkGreen: Color) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(playlists) { playlist ->
            PlaylistCard(playlist, darkGreen)
        }
    }
}

@Composable
fun PlaylistCard(playlist: PlaylistItem, darkGreen: Color) {
    Column(
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Badge(darkGreen, "읽는중")
                Spacer(modifier = Modifier.height(4.dp))
                Badge(Color.Gray, "완독")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = playlist.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        playlist.creator?.let {
            Text(
                text = it,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
// --- PlaylistComponent.kt 파일 하단에 아래 코드를 추가하세요 ---

/**
 * 개별 플레이리스트 카드 UI를 확인하기 위한 프리뷰 */
@Preview(name = "Single Playlist Card", showBackground = true)
@Composable
fun PlaylistCardPreview() {
    // 1. 프리뷰에서 사용할 가짜 데이터(PlaylistItem) 생성
    val mockPlaylist = PlaylistItem(
        playlistId = "1",
        title = "나만의 감성 플레이리스트",
        )

    // 테마를 적용하여 일관된 디자인을 확인합니다.
    Book_M_FrontTheme {
        // 프리뷰 확인 시 적절한 패딩을 줍니다.
        Column(modifier = Modifier.padding(16.dp)) {
            PlaylistCard(
                playlist = mockPlaylist,
                darkGreen = Color(0xFF1B5E20) // 실제 앱에서 사용하는 색상값으로 대체 가능
            )
        }
    }
}

/**
 * 플레이리스트가 가로로 나열되는 UI를 확인하기 위한 프리뷰
 */
@Preview(name = "Playlist Row", showBackground = true)
@Composable
fun PlaylistRowPreview() {
    // 1. 프리뷰에서 사용할 가짜 플레이리스트 목록 데이터 생성
    val mockPlaylists = listOf(
        PlaylistItem("1", "새벽 감성", "booklover123", ),
        PlaylistItem("2", "비 오는 날", "rainyday", ),
        PlaylistItem("3", "코딩할 때 듣는 Lo-Fi", "dev_cat", ),
        PlaylistItem("4", "산책하며", "walking_dog", )
    )

    // 테마를 적용하여 일관된 디자인을 확인합니다.
    Book_M_FrontTheme {
        // 프리뷰 확인 시 적절한 패딩을 줍니다.
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "추천 플레이리스트",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            PlaylistRow(
                playlists = mockPlaylists,
                darkGreen = Color(0xFF1B5E20) // 실제 앱에서 사용하는 색상값으로 대체 가능
            )
        }
    }
}
