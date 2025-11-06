package BukkeuBukkeu.Book_Eum.service.music;

import BukkeuBukkeu.Book_Eum.dto.music.AIMusicGenerateRequest;
import BukkeuBukkeu.Book_Eum.dto.music.AIMusicGenerateResponse;
import BukkeuBukkeu.Book_Eum.repository.AIMusicRepository;
import BukkeuBukkeu.Book_Eum.domain.Book;
import BukkeuBukkeu.Book_Eum.domain.AIMusic;
import BukkeuBukkeu.Book_Eum.external.MusicAi;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import BukkeuBukkeu.Book_Eum.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AIMusicGenerateService {

//    private final BookRepository bookRepository;
//    private final AIMusicRepository aiMusicRepository;
//    private final MusicAi musicAiClient;
//    private final FileStorageService fileStorageService;
//
//    @Transactional
//    public AIMusicGenerateResponse generateAiMusic(
//            String isbn,
//            Integer chapterNumber,
//            AIMusicGenerateRequest request
//    ) {
//        // 1) 도서 검증
//        Book book = bookRepository.findById(isbn)
//                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));
//
//        // 2) 프롬프트 검증 (DB가 아니라 request에서 받음)
//        String musicPrompt = request.getPrompt();
//        if (musicPrompt == null || musicPrompt.isBlank()) {
//            throw new IllegalArgumentException("musicPrompt must not be empty");
//        }
//
//        int durationSeconds = request.getDurationSeconds() != null
//                ? request.getDurationSeconds()
//                : 60; // 기본값
//
//        // 3) 음악 생성 AI 호출
//        byte[] audioData = musicAiClient.generateMusic(musicPrompt, durationSeconds);
//
//        // 4) 클라우드 업로드
//        String objectPath = String.format(
//                "music/%s/chap-%d-%d.mp3",
//                isbn, chapterNumber, System.currentTimeMillis()
//        );
//
//        String audioUrl = fileStorageService.uploadMusicFile(audioData, objectPath);
//
//        // 5) AiMusic 엔티티 저장
//        AIMusic aiMusic = AIMusic.builder()
//                .title("???") // 예시용
//                .audioFileUrl(audioUrl)
//                .moods("???") // 필요하면 FastAPI 분석 결과에서 가져와도 됨
//                .skipCnt(0)
//                .build();
//
//        aiMusicRepository.save(aiMusic);
//
//        // 6) 응답 DTO 반환
//        return new AIMusicGenerateResponse(
//                aiMusic.getId(),
//                book.getIsbn(),
//                chapterNumber,
//                aiMusic.getAudioFileUrl()
//        );
//    }

    private final AIMusicRepository aiMusicRepository;
    private final MusicAi musicAi;
    private final FileStorageService fileStorageService;

    @Transactional
    public AIMusicGenerateResponse generateAiMusic(
            String isbn,
            Integer chapterNumber,
            AIMusicGenerateRequest request
    ) {
        // 1) 프롬프트 검증
        String musicPrompt = request.getPrompt();
        if (musicPrompt == null || musicPrompt.isBlank()) {
            throw new IllegalArgumentException("musicPrompt must not be empty");
        }

        int durationSeconds = request.getDurationSeconds() != null
                ? request.getDurationSeconds()
                : 60; // 기본값

        // 2) 음악 생성 AI 호출
        byte[] audioData = musicAi.generateMusic(musicPrompt, durationSeconds);

        // 3) 파일 이름 결정 (중간발표용: 간단한 규칙)
        String fileName = String.format("%s-ch%d-%d.mp3",
                isbn, chapterNumber, System.currentTimeMillis());

        // 4) 파일 저장 (로컬 or 클라우드)
        String audioUrl = fileStorageService.uploadMusicFile(audioData, fileName);

        // 5) 제목/분위기 값 세팅
        // 제목 어케 정함???
//        String title = (request.getTitle() != null && !request.getTitle().isBlank())
//                ? request.getTitle()
//                : String.format("AI Music for %s (Chapter %d)", isbn, chapterNumber);

        // AIMusicGenerateRequest에 moods 없는데 만들까
//        String moods = (request.getMoods() != null && !request.getMoods().isBlank())
//                ? request.getMoods()
//                : "AI generated";

        // 6) AIMusic 엔티티 생성
        AIMusic aiMusic = AIMusic.builder()
                .title("???")
                .audioFileUrl(audioUrl)
                .moods("???")
                .skipCnt(0)
                .build();

        aiMusic = aiMusicRepository.save(aiMusic);

        // 7) 응답 DTO 반환
        return new AIMusicGenerateResponse(
                aiMusic.getId(),
                isbn,
                chapterNumber,
                aiMusic.getAudioFileUrl()
        );
    }
}
