package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.service.music.AIMusicGenerateService;
import BukkeuBukkeu.Book_Eum.dto.music.AIMusicGenerateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AIMusicController {

    private final AIMusicGenerateService aiMusicGenerateService;

    // 챕터별 AI 음악 생성 요청
//    @PostMapping("/books/chapters/ai-music")
//    public ResponseEntity<AIMusicGenerateResponse> generateAiMusic(
//            @PathVariable String isbn,
//            @PathVariable Integer chapterNum
//    ) {
//        AIMusicGenerateResponse response =
//                aiMusicGenerateService.generateAiMusic(isbn, chapterNum);
//        return ResponseEntity.ok(response);
//    }
}
