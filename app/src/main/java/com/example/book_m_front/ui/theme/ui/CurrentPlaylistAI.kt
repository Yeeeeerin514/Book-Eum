package com.example.book_m_front.ui.theme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.ui.theme.ui_resource.AppColors

data class Song(
    val title: String,
    val artist: String,
    val chapter: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen() {
    val songs = listOf(
        Song("음악 이름", "작곡가 | 김버마", "chapter 2 (1)"),
        Song("음악 이름", "작곡가 | 김버마", ""),
        Song("음악 이름", "작곡가 | 김버마", "chapter 2 (2)")
    )

    var sliderPosition by remember { mutableFloatStateOf(0.0f) }
    var isPlaying by remember { mutableStateOf(false) }

    Scaffold(

    ) { padding ->
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.White.copy(alpha = 0.3f))
                    .padding(padding)
                    .padding(top = 30.dp)
            ) {
                // 메인 플레이어 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.DeepGreen
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, start = 30.dp, end = 30.dp, bottom = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 헤더
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "플레이리스트 제목",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.8f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Chapter 제목
                        Text(
                            "Chapter 1",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 앨범 아트 (이미지 플레이스홀더)
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            // 새로고침 버튼
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-12).dp, y = 12.dp),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Color(0xFF2D5F4D),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 곡 정보
                        Text(
                            "음악 이름",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            "작곡가/연주자 : 김버마",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 재생 슬라이더
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            colors = SliderDefaults.colors(
                                thumbColor = AppColors.White,
                                activeTrackColor = AppColors.White,
                                inactiveTrackColor = AppColors.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 재생 버튼
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next songs 섹션
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Next songs",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Divider(
                            color = Color.White.copy(alpha = 0.4f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // 다음 곡 리스트
                        songs.forEach { song ->
                            SongItem(song)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }


@Composable
fun SongItem(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 썸네일
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 곡 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                song.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                song.artist,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            if (song.chapter.isNotEmpty()) {
                Text(
                    song.chapter,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}


@Preview
@Composable
fun MusicPlayerPreview() {
    MusicPlayerScreen()
}