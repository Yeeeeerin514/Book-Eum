package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.dto.book.ChapterAnalyzeResponse;
import BukkeuBukkeu.Book_Eum.service.book.ChapterAnalysisCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
@Slf4j
public class ChapterController {

    private final ChapterAnalysisCallbackService chapterAnalysisCallbackService;

    /**
     * AI 서버가 챕터 분석 결과를 한 개씩 콜백하는 엔드포인트
     */
    @PostMapping("/analyze/callback")
    public ResponseEntity<Void> saveChapterAnalysis(
            @RequestBody ChapterAnalyzeResponse dto
    ) {
        chapterAnalysisCallbackService.saveChapterAnalysis(dto);
        return ResponseEntity.ok().build();
    }
}
