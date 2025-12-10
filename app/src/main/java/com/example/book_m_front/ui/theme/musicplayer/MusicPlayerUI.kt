package com.example.book_m_front.ui.theme.musicplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel

// ============================================
// 6. UI - Jetpack Compose
// ============================================
/**
 * 🎵 통합 음악 플레이어 UI
 *
 * 두 화면을 포함:
 * 1. MusicPlayerScreen - 전체 화면 플레이어
 * 2. PlaylistScreen - 플레이리스트 목록
 */
// 메인 음악 플레이어 화면

@Composable
fun MusicPlayerUI(
    viewModel: MusicPlayerViewModel = viewModel()
) {
    // 현재 표시할 화면 (true = 플레이어, false = 플레이리스트)
    var showPlayer by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 플레이리스트 화면 (기본)
        PlaylistScreen(
            viewModel = viewModel,
            onTrackClick = { music ->
                viewModel.playTrack(music)
                showPlayer = true  // 플레이어 화면으로 전환
            }
        )

        // 전체 화면 플레이어 (위에 오버레이)
        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullMusicPlayerScreen(
                viewModel = viewModel,
                onClose = { showPlayer = false }  // 플레이리스트로 돌아가기
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: MusicPlayerViewModel,
    onTrackClick: (Music) -> Unit
) {
    val playlist by viewModel.playlist.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "플레이리스트",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2D5F4D),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 로딩 상태
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2D5F4D))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("플레이리스트 로딩 중...")
                    }
                }
                return@Scaffold
            }

            // 에러 상태
            errorMessage?.let { error ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            error,
                            color = Color.Red,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadPlaylist() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D5F4D)
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
                return@Scaffold
            }

            // 플레이리스트가 비어있음
            if (playlist.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "플레이리스트가 비어있습니다",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "음악을 추가해주세요",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                return@Scaffold
            }

            // 플레이리스트 목록
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(playlist) { index, music ->
                    PlaylistItem(
                        music = music,
                        isCurrentTrack = music.id == currentTrack?.id,
                        isPlaying = isPlaying && music.id == currentTrack?.id,
                        trackNumber = index + 1,
                        onClick = { onTrackClick(music) }
                    )
                }
            }

            // 미니 플레이어 (하단 고정)
            currentTrack?.let { track ->
                MiniPlayer(
                    track = track,
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onClick = onTrackClick
                )
            }
        }
    }
}


// ============================================
// 플레이리스트 아이템
// ============================================

@Composable
fun PlaylistItem(
    music: Music,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    trackNumber: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack)
                Color(0xFF2D5F4D).copy(alpha = 0.2f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentTrack) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 트랙 번호 또는 재생 아이콘
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentTrack && isPlaying) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color(0xFF2D5F4D),
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = trackNumber.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentTrack) Color(0xFF2D5F4D) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 앨범 아트 (작은 썸네일)
            AsyncImage(
                model = music.albumArtUrl,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 곡 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = music.title ?: "제목 없음",
                    fontSize = 16.sp,
                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentTrack) Color(0xFF2D5F4D) else Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = music.artist ?: "아티스트 없음",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (music.album != null) {
                    Text(
                        text = music.album,
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 더보기 버튼 (선택적)
            IconButton(onClick = { /* 메뉴 표시 */ }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.Gray
                )
            }
        }
    }
}

// ============================================
// 미니 플레이어 (하단 고정)
// ============================================

@Composable
fun MiniPlayer(
    track: Music,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClick: (Music) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(track) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D5F4D)
        ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 앨범 아트
            AsyncImage(
                model = track.albumArtUrl,
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 곡 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title ?: "제목 없음",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = track.artist ?: "아티스트 없음",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 재생/일시정지 버튼
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ============================================
// 2. 전체 화면 플레이어
// ============================================

@Composable
fun FullMusicPlayerScreen(
    viewModel: MusicPlayerViewModel,
    onClose: () -> Unit
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 닫기 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                "Now Playing",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            // 균형을 위한 빈 공간
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 앨범 커버 이미지
        AsyncImage(
            model = currentTrack?.albumArtUrl,
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 곡 정보
        Text(
            text = currentTrack?.title ?: "제목 없음",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentTrack?.artist ?: "아티스트 없음",
            fontSize = 18.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 재생 진행 바
        MusicProgressBar(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = { viewModel.seekTo(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 재생 컨트롤
        MusicControls(
            isPlaying = isPlaying,
            onPlayPause = { viewModel.togglePlayPause() },
            onPrevious = { viewModel.skipToPrevious() },
            onNext = { viewModel.skipToNext() }
        )

        // 버퍼링 인디케이터
        if (playerState is PlayerState.Buffering) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}


/*
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel = */
/*hiltViewModel*//*
viewModel()
) {
    // ViewModel의 StateFlow를 State로 변환 (자동 UI 업데이트)
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 앨범 커버 이미지
        // Coil 라이브러리가 URL에서 이미지를 자동으로 다운로드해서 표시
        AsyncImage(
            model = currentTrack?.albumArtUrl, // 서버의 이미지 URL
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 곡 제목
        Text(
            text = currentTrack?.title ?: "제목 없음",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 아티스트명
        Text(
            text = currentTrack?.artist ?: "아티스트 없음",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 재생 진행 바 (슬라이더)
        MusicProgressBar(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = { viewModel.seekTo(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 재생 컨트롤 버튼들
        MusicControls(
            isPlaying = isPlaying,
            onPlayPause = { viewModel.togglePlayPause() },
            onPrevious = { viewModel.skipToPrevious() },
            onNext = { viewModel.skipToNext() }
        )

        // 버퍼링 중일 때 로딩 인디케이터 표시
        if (playerState is PlayerState.Buffering) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 16.dp),
                color = Color.White
            )
        }
    }
}
*/

// ============================================
// 재생 진행 바
// ============================================

@Composable
fun MusicProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var tempSliderValue by remember { mutableStateOf<Float?>(null) }

    Column {
        Slider(
            value = tempSliderValue ?: currentPosition.toFloat(),
            onValueChange = { tempSliderValue = it },
            onValueChangeFinished = {
                tempSliderValue?.let { onSeek(it.toLong()) }
                tempSliderValue = null
            },
            valueRange = 0f..duration.coerceAtLeast(1).toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Gray
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = formatTime(duration),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}



// ============================================
// 재생 컨트롤 버튼
// ============================================

@Composable
fun MusicControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이전 곡
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        // 재생/일시정지
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(72.dp)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        // 다음 곡
        IconButton(onClick = onNext) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}


// ============================================
// 7. 유틸리티 함수
// ============================================

// 밀리초를 "분:초" 형식으로 변환
// 예: 90000 → "1:30"
fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}









//--------------------------------Preview


// --- MusicPlayerUI.kt 파일 하단에 이 코드를 추가하거나 기존 코드를 대체하세요 ---

/**
 * 프리뷰에서 사용할 가짜 음악 데이터 목록
 */
private val samplePlaylist = listOf(
    Music("1", "사건의 지평선", "윤하", "https://i.scdn.co/image/ab67616d0000b273c4033f2d0d2a84a2a3e6c3e0", ""),
    Music("2", "Hype Boy", "NewJeans", "https://i.scdn.co/image/ab67616d0000b27318a03f488d57865c2763c298", ""),
    Music("3", "LOVE DIVE", "IVE (아이브)", "https://i.scdn.co/image/ab67616d0000b273a2dd49b88f8324671a56f296", ""),
    Music("4", "긴 제목 테스트: 이 노래의 제목은 화면을 넘어갈 정도로 아주 깁니다", "긴 아티스트 이름", "https://i.scdn.co/image/ab67616d0000b273a3a8a3a0d4b2b3a0d4b2b3a0", "")
)

// --- PlaylistScreen 관련 프리뷰 ---
/*

@Preview(name = "Playlist - Loaded", showBackground = true)
@Composable
fun PlaylistScreenPreview_Loaded() {
    // 가짜 ViewModel을 만들고 상태를 직접 설정합니다.
    val viewModel: MusicPlayerViewModel = viewModel()
    viewModel.playlist.value = samplePlaylist
    viewModel.currentTrack.value = samplePlaylist[1] // "Hype Boy"를 현재 곡으로 설정
    viewModel.isPlaying.value = true
    viewModel.isLoading.value = false
    viewModel.errorMessage.value = null

    PlaylistScreen(viewModel = viewModel, onTrackClick = {})
}

@Preview(name = "Playlist - Loading", showBackground = true)
@Composable
fun PlaylistScreenPreview_Loading() {
    val viewModel: MusicPlayerViewModel = viewModel()
    viewModel.isLoading.value = true
    viewModel.errorMessage.value = null
    viewModel.playlist.value = emptyList()

    PlaylistScreen(viewModel = viewModel, onTrackClick = {})
}

@Preview(name = "Playlist - Error", showBackground = true)
@Composable
fun PlaylistScreenPreview_Error() {
    val viewModel: MusicPlayerViewModel = viewModel()
    viewModel.isLoading.value = false
    viewModel.errorMessage.value = "네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    viewModel.playlist.value = emptyList()

    PlaylistScreen(viewModel = viewModel, onTrackClick = {})
}

@Preview(name = "Playlist - Empty", showBackground = true)
@Composable
fun PlaylistScreenPreview_Empty() {
    val viewModel: MusicPlayerViewModel = viewModel()
    viewModel.isLoading.value = false
    viewModel.errorMessage.value = null
    viewModel.playlist.value = emptyList()

    PlaylistScreen(viewModel = viewModel, onTrackClick = {})
}
*/


// --- 개별 컴포넌트 프리뷰 ---

@Preview(name = "PlaylistItem - Playing", showBackground = true)
@Composable
fun PlaylistItemPreview_Playing() {
    PlaylistItem(
        music = samplePlaylist[0],
        isCurrentTrack = true,
        isPlaying = true,
        trackNumber = 1,
        onClick = {}
    )
}

@Preview(name = "PlaylistItem - Normal", showBackground = true)
@Composable
fun PlaylistItemPreview_Normal() {
    PlaylistItem(
        music = samplePlaylist[1],
        isCurrentTrack = false,
        isPlaying = false,
        trackNumber = 2,
        onClick = {}
    )
}

@Preview(name = "MiniPlayer - Playing", showBackground = true)
@Composable
fun MiniPlayerPreview_Playing() {
    MiniPlayer(
        track = samplePlaylist[0],
        isPlaying = true,
        onPlayPause = {},
        onClick = {}
    )
}

@Preview(name = "MiniPlayer - Paused", showBackground = true)
@Composable
fun MiniPlayerPreview_Paused() {
    MiniPlayer(
        track = samplePlaylist[1],
        isPlaying = false,
        onPlayPause = {},
        onClick = {}
    )
}
