package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import BukkeuBukkeu.Book_Eum.dto.music.ChapterPlaylistResponse;
import BukkeuBukkeu.Book_Eum.service.music.MusicService;
import BukkeuBukkeu.Book_Eum.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private final MusicService musicService;
    private final FileStorageService fileStorageService;

    /**
     * 🎯 1. 책의 챕터별 플레이리스트 조회
     *
     * GET /music/playlist/{isbn}
     *
     * 예시: GET /music/playlist/9788934942467
     *
     * @param isbn 책 ISBN
     * @return 챕터별로 구성된 전체 플레이리스트 메타데이터
     */
    @GetMapping("/playlist/{isbn}")
    public ResponseEntity<ChapterPlaylistResponse> getPlaylistByIsbn(
            @PathVariable String isbn
    ) {
        try {
            log.info("플레이리스트 조회 요청: ISBN={}", isbn);

            ChapterPlaylistResponse response = musicService.getChapterPlaylistByIsbn(isbn);

            log.info("플레이리스트 조회 성공: ISBN={}, 챕터 수={}, 음악 수={}",
                    isbn, response.getTotalChapters(), response.getTotalTracks());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("플레이리스트 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("플레이리스트 조회 중 오류 발생: ISBN={}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎯 2. 음악 파일 다운로드
     *
     * GET /music/{musicId}/download
     *
     * 예시: GET /music/123/download
     *
     * @param musicId 음악 ID
     * @return 음악 파일 (MP3 바이너리)
     */
    @GetMapping("/{musicId}/download")
    public ResponseEntity<Resource> downloadMusicFile(
            @PathVariable Long musicId
    ) {
        try {
            log.info("음악 다운로드 요청: musicId={}", musicId);

            // 1. 음악 정보 조회
            AIMusic music = musicService.findById(musicId);

            if (music == null || music.getAudioFilePath() == null) {
                log.warn("음악 파일 경로 없음: musicId={}", musicId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 2. 파일 경로 가져오기
            String audioFilePath = music.getAudioFilePath();
            Path filePath = Paths.get(audioFilePath).normalize();

            // 3. 파일 존재 확인
            if (!Files.exists(filePath)) {
                log.warn("음악 파일이 존재하지 않음: path={}", audioFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 4. Resource 생성
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("음악 파일을 읽을 수 없음: path={}", audioFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 5. Content-Type 자동 감지
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "audio/mpeg";  // 기본값: MP3
            }

            // 6. 파일명 생성 (안전화)
            String fileName = sanitizeFileName(music.getTitle()) + ".mp3";

            log.info("음악 다운로드 성공: musicId={}, title={}, size={} bytes",
                    musicId, music.getTitle(), resource.contentLength());

            // 7. 응답 반환
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH,
                            String.valueOf(resource.contentLength()))
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.warn("음악을 찾을 수 없음: musicId={}", musicId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (MalformedURLException e) {
            log.error("잘못된 파일 경로: musicId={}", musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (IOException e) {
            log.error("파일 읽기 오류: musicId={}", musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            log.error("음악 다운로드 중 예외 발생: musicId={}", musicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🎯 3. 앨범 아트 다운로드 (선택적)
     *
     * GET /music/{musicId}/artwork
     *
     * @param musicId 음악 ID
     * @return 앨범 아트 이미지
     */
    @GetMapping("/{musicId}/artwork")
    public ResponseEntity<Resource> getAlbumArtwork(
            @PathVariable Long musicId
    ) {
        // 앨범 아트 기능이 필요하면 구현
        // 현재는 기본 이미지 반환 또는 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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