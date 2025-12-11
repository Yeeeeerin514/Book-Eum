package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.service.music.MusicFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * 🎵 음악 API 컨트롤러
 *
 * 엔드포인트:
 * 1. GET /music/playlist/{isbn} - 챕터별 플레이리스트 조회
 * 2. GET /music/{musicId}/download - 음악 파일 다운로드
 */
@Slf4j
@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicFileService musicFileService;

    /**
     * 음악 파일 다운로드 (.wav)
     * GET /music/{musicId}/download
     */
    @GetMapping(value = "/{musicId}/download", produces = "audio/wav")
    public ResponseEntity<Resource> downloadMusic(@PathVariable Long musicId) {

        MusicFileService.MusicFile musicFile = musicFileService.getMusicFile(musicId);

        // 파일명 인코딩
        String encodedName = UriUtils.encode(musicFile.fileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .contentLength(musicFile.fileSize())
                .body(musicFile.resource());
    }

    /**
     * 파일명 안전화 (특수문자 제거)
     *
     * @param fileName 원본 파일명
     * @return 안전한 파일명
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "music";
        }

        // 파일명에서 사용할 수 없는 문자 제거
        String safe = fileName.replaceAll("[^a-zA-Z0-9가-힣\\s\\-_.]", "")
                .replaceAll("\\s+", "_");

        // 최대 길이 제한
        if (safe.length() > 100) {
            safe = safe.substring(0, 100);
        }

        return safe.isEmpty() ? "music" : safe;
    }
}