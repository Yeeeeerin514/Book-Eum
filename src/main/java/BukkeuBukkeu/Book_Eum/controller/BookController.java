package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.dto.book.*;
import BukkeuBukkeu.Book_Eum.service.book.BookAnalysisService;
import BukkeuBukkeu.Book_Eum.service.book.BookRegisterService;
import BukkeuBukkeu.Book_Eum.service.book.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookAnalysisService bookAnalysisService;
    private final BookRegisterService bookRegisterService;

    /**
     * 제목으로 도서 검색
     */
    @GetMapping("/search")
    public BookSearchResponse searchByTitle(@RequestParam String query,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookService.searchByTitle(query, pageable);
    }

    /**
     * 도서 등록
     * 관리자나 유저가 epub 파일을 업로드해서 도서를 등록할 때 사용
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerBook(
            @Valid @ModelAttribute BookRegisterRequest request
    ) {
        bookRegisterService.register(request);
        // “정상적으로 등록됐다”고만 간단히 알려줌
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Book registered successfully");
    }

    /**
     * 도서 내용 분석 요청 API
     * PathVariable: isbn
     * RequestBody: BookAnalyzeRequest (epubFileUrl은 중간 발표에서는 무시)
     */
    @PostMapping("/{isbn}/analyze")
    public ResponseEntity<BookAnalyzeResponse> analyzeBook(
            @PathVariable String isbn,
            @RequestBody(required = false) BookAnalyzeRequest request
    ) {

        // 중간 발표에서는 request.epubFileUrl은 사용 안 하고 로컬 파일 사용
        BookAnalyzeResponse response = bookAnalysisService.analyzeBook(isbn);
        return ResponseEntity.ok(response);
    }

    /**
     * 도서 삭제
     */
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {

        bookService.deleteByIsbn(isbn);
        return ResponseEntity.noContent().build();
    }
}
