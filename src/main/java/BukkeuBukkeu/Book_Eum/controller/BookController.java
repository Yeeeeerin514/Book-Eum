package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.dto.book.*;
import BukkeuBukkeu.Book_Eum.service.book.BookAnalysisService;
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
     * ✅ 새로 추가: ISBN으로 EPUB 파일 다운로드
     *
     * GET /books/{isbn}/download
     *
     * 예시: GET /books/9788934942467/download
     *
     * @param isbn 책 ISBN
     * @return EPUB 파일 (application/epub+zip)
     */
    @GetMapping("/{isbn}/download")
    public ResponseEntity<Resource> downloadEpubFile(@PathVariable String isbn) {
        try {
            log.info("EPUB 다운로드 요청: ISBN={}", isbn);

            // 1. ISBN으로 도서 정보 조회
            Book book = bookService.getByIsbn(isbn);

            if (book == null) {
                log.warn("책을 찾을 수 없음: ISBN={}", isbn);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 2. EPUB 파일 경로 가져오기
            String epubFilePath = book.getEpubFilePath();

            if (epubFilePath == null || epubFilePath.isEmpty()) {
                log.warn("EPUB 파일 경로 없음: ISBN={}", isbn);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 3. 파일 경로를 Path 객체로 변환
            Path filePath = Paths.get(epubFilePath).normalize();

            // 4. 파일이 실제로 존재하는지 확인
            if (!Files.exists(filePath)) {
                log.warn("EPUB 파일이 존재하지 않음: path={}", epubFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 5. Resource 객체 생성
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("EPUB 파일을 읽을 수 없음: path={}", epubFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 6. Content-Type 설정
            String contentType = "application/epub+zip";

            // 7. 파일명 생성 (한글 제목 처리)
            String fileName = sanitizeFileName(book.getTitle()) + ".epub";

            log.info("EPUB 다운로드 성공: ISBN={}, title={}, size={} bytes",
                    isbn, book.getTitle(), resource.contentLength());

            // 8. HTTP 응답 헤더 설정
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH,
                            String.valueOf(resource.contentLength()))
                    .body(resource);

        } catch (Exception e) {
            log.error("EPUB 다운로드 중 오류 발생: ISBN={}", isbn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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