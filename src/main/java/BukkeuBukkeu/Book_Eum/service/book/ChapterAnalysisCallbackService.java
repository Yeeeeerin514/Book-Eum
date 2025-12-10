package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.book.Chapter;
import BukkeuBukkeu.Book_Eum.domain.book.ChapterAnalysis;
import BukkeuBukkeu.Book_Eum.dto.book.ChapterAnalyzeResponse;
import BukkeuBukkeu.Book_Eum.repository.ChapterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChapterAnalysisCallbackService {

    private final ChapterRepository chapterRepository;

    /**
     * AI 서버가 보내 준 한 챕터 분석 결과를
     * Chapter 엔티티로 변환해서 저장하는 서비스 메서드
     */
    @Transactional
    public void saveChapterAnalysis(ChapterAnalyzeResponse dto) {
        String isbn = dto.getIsbn();
        int chapterNum = dto.getChapterNumber();

        // DTO -> ChapterAnalysis (JSON으로 저장될 임베디드 객체)
        ChapterAnalysis analysis = ChapterAnalysis.builder()
                .mainMood(dto.getMainMood())
                .emotions(dto.getEmotions())
                .genres(dto.getGenres())
                .instruments(dto.getInstruments())
                .tempo(dto.getTempo())
                .keywords(dto.getKeywords())
                .build();

        // PK (isbn, chapterNum)가 같으면 update 역할
        Chapter chapter = Chapter.builder()
                .isbn(isbn)
                .chapterNum(chapterNum)
                .analysis(analysis)
                .build();

        chapterRepository.save(chapter);

        log.info("[BookAnalysis] 챕터 분석 저장 완료 isbn={} chapter={}", isbn, chapterNum);
    }
}
