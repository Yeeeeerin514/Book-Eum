package com.example.book_m_front.ui.theme.musicplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.book_m_front.network.dto.Music

// ============================================
// 6. UI - Jetpack Compose
// ============================================

// 메인 음악 플레이어 화면
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel = /*hiltViewModel*/viewModel()
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

// 재생 진행 바 컴포넌트
@Composable
fun MusicProgressBar(
    currentPosition: Long,  // 현재 위치 (밀리초)
    duration: Long,          // 전체 길이 (밀리초)
    onSeek: (Long) -> Unit   // 슬라이더 드래그 완료 시 호출될 함수
) {
    // 사용자가 슬라이더를 드래그하는 동안의 임시 값
    var tempSliderValue by remember { mutableStateOf<Float?>(null) }

    Column {
        // 슬라이더 (진행 바)
        Slider(
            // 현재 표시할 값: 드래그 중이면 임시 값, 아니면 실제 재생 위치
            value = tempSliderValue ?: currentPosition.toFloat(),
            // 슬라이더를 드래그할 때
            onValueChange = { tempSliderValue = it },
            // 슬라이더 드래그가 끝났을 때
            onValueChangeFinished = {
                tempSliderValue?.let { onSeek(it.toLong()) } // 실제로 위치 이동
                tempSliderValue = null // 임시 값 초기화
            },
            valueRange = 0f..duration.toFloat(), // 슬라이더 범위
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Gray
            )
        )

        // 현재 시간 / 전체 시간 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition), // 예: "1:30"
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = formatTime(duration), // 예: "3:45"
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

// 재생 컨트롤 버튼들 (이전/재생/다음)
@Composable
fun MusicControls(
    isPlaying: Boolean,         // 현재 재생 중인지
    onPlayPause: () -> Unit,    // 재생/일시정지 버튼 클릭
    onPrevious: () -> Unit,     // 이전 곡 버튼 클릭
    onNext: () -> Unit          // 다음 곡 버튼 클릭
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이전 곡 버튼
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        // 재생/일시정지 버튼 (중앙, 큰 버튼)
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(72.dp)
                .background(Color.White, CircleShape) // 흰색 원형 배경
        ) {
            Icon(
                // isPlaying이 true면 일시정지 아이콘, false면 재생 아이콘
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        // 다음 곡 버튼
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



/**
 * 미리보기용 가짜 데이터
 */
private val sampleTrack = Music(
    id = "1",
    title = "B 612",
    artist = "오왠(O.WHEN)",
    album = "어린왕자 OST",
    audioUrl = "http://example.com/music.mp3",
    albumArtUrl = "https://i.scdn.co/image/ab67616d0000b273a3a8a3a0d4b2b3a0d4b2b3a0" // 실제 이미지 URL 예시
)

/**
 * MusicPlayerScreen 전체 미리보기 (재생 중 상태)
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MusicPlayerScreenPreview_Playing() {
    // 가짜 ViewModel을 만들거나, 상태 값을 직접 전달하는 방식을 사용합니다.
    // 여기서는 상태 값을 직접 전달하는 방식으로 각 컴포넌트를 조합합니다.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        AsyncImage(
            model = sampleTrack.albumArtUrl,
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = sampleTrack.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = sampleTrack.artist,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        MusicProgressBar(
            currentPosition = 90000, // 1분 30초
            duration = 225000,       // 3분 45초
            onSeek = {}
        )
        Spacer(modifier = Modifier.height(32.dp))
        MusicControls(
            isPlaying = true, // 재생 중인 상태
            onPlayPause = {},
            onPrevious = {},
            onNext = {}
        )
    }
}

/**
 * MusicPlayerScreen 전체 미리보기 (일시정지 상태)
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MusicPlayerScreenPreview_Paused() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (위의 UI 구성과 동일)
        Spacer(modifier = Modifier.height(40.dp))
        AsyncImage(
            model = sampleTrack.albumArtUrl,
            contentDescription = "Album Cover",
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "곡 제목이 길어질 경우 이렇게 표시됩니다",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "아티스트",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        MusicProgressBar(
            currentPosition = 60000,
            duration = 180000,
            onSeek = {}
        )
        Spacer(modifier = Modifier.height(32.dp))
        MusicControls(
            isPlaying = false, // 일시정지 상태
            onPlayPause = {},
            onPrevious = {},
            onNext = {}
        )
        // 버퍼링 상태 미리보기
        CircularProgressIndicator(
            modifier = Modifier.padding(top = 16.dp),
            color = Color.White
        )
    }
}


/**
 * ProgressBar 컴포넌트 단독 미리보기
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MusicProgressBarPreview() {
    MusicProgressBar(
        currentPosition = 90000, // 1분 30초
        duration = 225000,       // 3분 45초
        onSeek = { }
    )
}

/**
 * 컨트롤 버튼 컴포넌트 단독 미리보기 (재생 중)
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MusicControlsPreview_Playing() {
    MusicControls(
        isPlaying = true,
        onPlayPause = { },
        onPrevious = { },
        onNext = { }
    )
}

/**
 * 컨트롤 버튼 컴포넌트 단독 미리보기 (일시정지)
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MusicControlsPreview_Paused() {
    MusicControls(
        isPlaying = false,
        onPlayPause = { },
        onPrevious = { },
        onNext = { }
    )
}