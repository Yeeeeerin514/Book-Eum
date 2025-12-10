package BukkeuBukkeu.Book_Eum.service.music;

import BukkeuBukkeu.Book_Eum.domain.book.*;
import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import BukkeuBukkeu.Book_Eum.domain.music.AIMusicAnalysis;
import BukkeuBukkeu.Book_Eum.dto.music.AIMusicGenerateRequest;
import BukkeuBukkeu.Book_Eum.dto.music.AIMusicGenerateResponse;
import BukkeuBukkeu.Book_Eum.repository.AIMusicRepository;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import BukkeuBukkeu.Book_Eum.repository.ChapterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIMusicGenerateService {

    private final AIMusicRepository aiMusicRepository;
    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    private static final String GCP_IP = "35.227.17.235"; // GCP 인스턴스의 [외부 IP] !! 고정인가?
    private static final String GCP_URL = "http://" + GCP_IP + ":8000/books/chapters/ai-music";
    private static final String MUSIC_POOL_PATH = "/Users/luna/CAU/2025-2/Capston Design 1/Book-Eum/data/music";

    @Async // 비동기 실행 (사용자 대기 방지)
    @Transactional // DB 상태 변경 보장
    public void requestGenerateMusicAsync(String isbn, Integer chapterNum) {

        // 1. 책 정보 조회 및 상태 업데이트 (시작)
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found: " + isbn));

        // 아직 음악 생성이 시작되지 않은 상태라면 '진행 중'으로 변경
        if (book.getStatus() == Status.ANALYSIS_COMPLETED) {
            book.markGenerationProcessing(); // Status.GENERATION_PROCESSING
            bookRepository.save(book);
            log.info("📘 책 '{}'의 상태를 GENERATION_PROCESSING으로 변경했습니다.", book.getTitle());
        }

        // 2. 챕터 및 분석 데이터 조회
        ChapterId chapterId = new ChapterId(isbn, chapterNum);
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + isbn + "-" + chapterNum));

        ChapterAnalysis analysis = chapter.getAnalysis();
        if (analysis == null) {
            log.warn("⚠️ [Skip] 분석 데이터 없음: {} - CH{}", isbn, chapterNum);
            return;
        }

        log.info("🚀 음악 생성 요청 시작: ISBN={} CH={}", isbn, chapterNum);

        // 3. 요청 DTO 생성
        AIMusicGenerateRequest requestDto = AIMusicGenerateRequest.builder()
                .isbn(chapter.getIsbn())
                .chapterNumber(chapter.getChapterNum())
                .mainMood(analysis.getMainMood())
                .emotions(analysis.getEmotions())
                .genres(analysis.getGenres())
                .instruments(analysis.getInstruments())
                .tempo(analysis.getTempo())
                .keywords(analysis.getKeywords())
                .build();

        // 5. RestTemplate 콜백 설정
        RequestCallback requestCallback = request -> {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            new ObjectMapper().writeValue(request.getBody(), requestDto);
        };

        ResponseExtractor<Boolean> responseExtractor = response -> {
            // GCP에서 500 등 에러가 왔을 때
            if (response.getStatusCode().isError()) {
                log.error("❌ GCP 서버 에러: {}", response.getStatusCode());
                return false;
            }

            // 5-1. 헤더에서 메타데이터 꺼내기
            String encodedMetadata = response.getHeaders().getFirst("X-Music-Metadata");
            if (encodedMetadata == null) {
                log.error("❌ X-Music-Metadata 헤더가 없습니다.");
                return false;
            }

            try {
                // URL 디코딩
                String decodedJson = URLDecoder.decode(encodedMetadata, StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();

                // JSON → AIMusicGenerateResponse
                AIMusicGenerateResponse meta =
                        objectMapper.readValue(decodedJson, AIMusicGenerateResponse.class);

                // 5-2. 오디오 파일 저장
                // FastAPI에서 이미 filename = f"{isbn}_{chapter}.wav" 로 주고 있으므로,
                // 자바 쪽에서도 동일 규칙 사용
                String filename = meta.getIsbn() + "_" + meta.getChapterNumber() + ".wav";
                Path dir = Paths.get(MUSIC_POOL_PATH);
                Files.createDirectories(dir); // 디렉토리 없으면 생성

                Path targetPath = dir.resolve(filename);
                Files.copy(response.getBody(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("✅ 음악 파일 저장 완료 (Pool): {}", targetPath);

                // DTO에 파일 정보 세팅
                meta.setFilename(filename);
                meta.setAudioFilePath(targetPath.toString());

                // 5-3. AIMusic 엔티티로 변환해서 저장
                saveAIMusicEntity(meta, book);

                return true;

            } catch (Exception e) {
                log.error("❌ 응답 처리 중 에러: {}", e.getMessage(), e);
                return false;
            }
        };

        try {
            // 6. 실행 (GCP 호출)
            Boolean isSuccess = restTemplate.execute(GCP_URL, HttpMethod.POST, requestCallback, responseExtractor);

            // 7. 성공 시 마지막 챕터 체크
            if (Boolean.TRUE.equals(isSuccess)) {
                checkAndCompleteBook(book, chapterNum);
            } else {
                // HTTP 통신은 성공했으나 내부 로직상 false 반환된 경우 (거의 없음)
                handleFailure(book);
            }

        } catch (Exception e) {
            log.error("❌ 통신 중 에러 발생: {}", e.getMessage());
            handleFailure(book);
        }
    }

    private void saveAIMusicEntity(AIMusicGenerateResponse meta, Book book) {

        // 1. 메타데이터 → AIMusicAnalysis
        AIMusicAnalysis analysis = meta.toAnalysis();

        // 2. AIMusic 엔티티 생성
        // AllArgsConstructor 존재하니까 id/null, playlists는 빈 리스트로 넣어도 됨
        AIMusic aiMusic = new AIMusic(
                null,                               // id (auto increment)
                meta.getFilename(),                    // title
                meta.getAudioFilePath(),               // audioFilePath
                analysis,                              // analysis (JSON 컬럼)
                0,                                     // skipCnt 초기값
                new ArrayList<>()                      // playlists
        );

        aiMusicRepository.save(aiMusic);

        log.info("🎵 AIMusic 저장 완료: book='{}', chapter={}, file='{}'",
                book.getTitle(), meta.getChapterNumber(), meta.getAudioFilePath());
    }

    // 🔍 마지막 챕터인지 확인하고 완료 처리하는 메서드
    private void checkAndCompleteBook(Book book, Integer currentChapterNum) {
        Integer totalChapters = book.getTotalChapters();

        // 총 챕터 수가 설정되어 있고, 현재 챕터가 마지막 번호라면
        if (totalChapters != null && totalChapters.equals(currentChapterNum)) {

            log.info("🎉 마지막 챕터(CH{}) 음악 생성 완료! 책 '{}'의 상태를 COMPLETED로 변경합니다.", currentChapterNum, book.getTitle());

            book.markCompleted(); // Status.COMPLETED
            bookRepository.save(book);
        }
    }

    // 🚨 실패 처리 메서드
    private void handleFailure(Book book) {
        // 이미 실패 상태가 아니라면 실패로 변경
        if (book.getStatus() != Status.GENERATION_FAILED) {
            book.markGenerationFailed(); // Status.GENERATION_FAILED
            bookRepository.save(book);
            log.error("🔥 책 '{}'의 상태를 GENERATION_FAILED로 변경했습니다.", book.getTitle());
        }
    }
}
