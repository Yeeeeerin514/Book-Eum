package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.domain.Book;
import BukkeuBukkeu.Book_Eum.dto.book.*;
import BukkeuBukkeu.Book_Eum.service.book.BookAnalysisService;
import BukkeuBukkeu.Book_Eum.service.book.BookRegisterService;
import BukkeuBukkeu.Book_Eum.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * 도서 등록 (multipart/form-data)
     * 관리자나 유저가 epub 파일을 업로드해서 도서를 등록할 때 사용
     */
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<BookResponse> registerBook(
            @RequestParam("isbn") String isbn,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("plot") String plot,
            @RequestPart("epubFile") MultipartFile epubFile) {

        Book newBook = bookRegisterService.registerBookWithEpub(isbn, title, author, plot, epubFile);
        BookResponse response = BookResponse.fromEntity(newBook);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 제목으로 도서 검색
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<BookSearchResponse>> searchBooksByTitle(
            @RequestParam("keyword") String keyword) {

        List<BookSearchResponse> results = bookService.searchBooksByTitle(keyword)
                .stream()
                .map(BookSearchResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * 장르로 도서 검색
     */
    @GetMapping("/search/genre")
    public ResponseEntity<List<BookSearchResponse>> searchBooksByGenre(
            @RequestParam("genre") String genre) {

        List<BookSearchResponse> results = bookService.searchBooksByGenre(genre)
                .stream()
                .map(BookSearchResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
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
