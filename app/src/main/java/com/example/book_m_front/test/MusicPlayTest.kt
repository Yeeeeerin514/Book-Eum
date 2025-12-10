package com.example.book_m_front.test

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.book_m_front.network.dto.Music
import com.example.book_m_front.ui.theme.viewmodel.MusicPlayerViewModel

/**
 * 로컬 오디오 파일을 가져와 플레이리스트를 구성하고 재생을 테스트하는 화면입니다.
 */
@Composable
fun LocalPlaylistTestScreen(
    // 실제 앱에서는 Hilt를 통해 주입받지만, 테스트를 위해 매개변수로 받습니다.
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    // 1. 상태 관리
    // 사용자가 선택한 음악 파일들로 만들어진 Track 리스트를 저장합니다.
    var localPlaylist by remember { mutableStateOf<List<Music>>(emptyList()) }
    val context = LocalContext.current

    // 현재 재생 중인 트랙의 상태를 ViewModel로부터 관찰합니다.
    val currentTrack by musicPlayerViewModel.currentTrack.collectAsState()

    // 권한 요청 추가
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // 권한이 거부되었을 때의 처리
        }
    }
    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // 2. 다중 파일 선택기 (Launcher)
    // 사용자가 여러 오디오 파일을 선택하면 그 결과(Uri 목록)를 받아 처리합니다.
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                // 선택된 URI 목록을 Track 객체 리스트로 변환합니다.
                val newTracks = uris.mapIndexed { index, uri ->
                    Music(
                        id = index.toString(), // 임시 ID 부여
                        title = uri.lastPathSegment ?: "Unknown Title", // 파일명으로 제목 설정
                        artist = "Local Artist", // 임시 아티스트명
                        albumArtUrl = null, // 로컬 파일이므로 앨범 아트는 없음
                        audioUrl = uri.toString(), // 가장 중요! 오디오 재생을 위한 URI
                        album = "Local Album", // 임시 앨범명
                    )
                }
                localPlaylist = newTracks
                // 변환된 트랙 목록을 ViewModel의 플레이리스트로 설정합니다.
                musicPlayerViewModel.setPlaylist(newTracks)
            }
        }
    )

    // 3. UI 렌더링
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("로컬 음악 플레이리스트 테스트", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                // "audio/*" MIME 타입을 사용하여 오디오 파일만 필터링합니다.
                audioPickerLauncher.launch(arrayOf("audio/*"))
            }) {
                Text("음악 파일 선택하기")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 플레이리스트가 비어있지 않으면 목록을 표시합니다.
            if (localPlaylist.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(localPlaylist) { index, track ->
                        PlaylistItemRow(
                            track = track,
                            isPlaying = track.id == currentTrack?.id, // 현재 재생 중인 곡인지 확인
                            onItemClick = {
                                // 항목 클릭 시, ViewModel에 해당 인덱스의 곡을 재생하도록 요청합니다.
                                musicPlayerViewModel.playTrack(track)
                            }
                        )
                    }
                }
            } else {
                Text("선택된 음악 파일이 없습니다.")
            }
        }
    }
}

/**
 * 플레이리스트의 각 항목을 표시하는 Composable
 */
@Composable
fun PlaylistItemRow(
    track: Music,
    isPlaying: Boolean,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onItemClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title ?: "제목 없음",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
                )
                Text(
                    text = track.artist ?: "아티스트 정보 없음",
                    fontSize = 14.sp,
                    color = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.Gray
                )
            }
            if (isPlaying) {
                Text("▶", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
