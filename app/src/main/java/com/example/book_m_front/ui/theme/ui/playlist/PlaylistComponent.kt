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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.network.dto.PlaylistItem
import com.example.book_m_front.ui.theme.ui.badge.Badge

//데이터 패키지로 따로 관리하는 게 좋을 수도
/*data class PlaylistItem(//흠 뭐 id같은거 필요없나?
    val title: String,
    val creator: String
)*/
@Composable
fun PlaylistRow(playlists: List<com.example.book_m_front.network.dto.PlaylistItem>, darkGreen: Color) {
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