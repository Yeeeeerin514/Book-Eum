package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.dto.book.*;
import BukkeuBukkeu.Book_Eum.service.book.BookAnalysisService;
import BukkeuBukkeu.Book_Eum.service.book.BookContentService;
import BukkeuBukkeu.Book_Eum.service.book.BookRegisterService;
import BukkeuBukkeu.Book_Eum.service.book.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 📚 도서 API 컨트롤러
 *
 * 추가된 기능:
 * - GET /books/{isbn}/download - EPUB 파일 다운로드
 */
@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookAnalysisService bookAnalysisService;
    private final BookRegisterService bookRegisterService;
    private final BookContentService bookContentService;

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
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Book registered successfully");
    }

    /**
     * 도서 epub 파일 다운로드
     */
    @GetMapping(value = "/{isbn}/content", produces = "application/epub+zip")
    public ResponseEntity<Resource> downloadBook(@PathVariable String isbn) {
        BookContentService.BookFile bookFile = bookContentService.getBookFile(isbn);

        // 한글 파일명 깨지는 것 방지
        String encodedFileName = UriUtils.encode(bookFile.fileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.parseMediaType("application/epub+zip"))
                .contentLength(bookFile.contentLength())
                .body(bookFile.resource());
    }

    /**
     * 도서 삭제
     */
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        bookService.deleteByIsbn(isbn);
        return ResponseEntity.noContent().build();
    }

    /**
     * 파일명 안전화 (특수문자 제거)
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "book";
        }

        return fileName.replaceAll("[^a-zA-Z0-9가-힣\\s\\-_.]", "")
                .replaceAll("\\s+", "_")
                .substring(0, Math.min(fileName.length(), 100));
    }
}