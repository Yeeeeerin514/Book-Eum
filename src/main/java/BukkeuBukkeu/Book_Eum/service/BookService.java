package BukkeuBukkeu.Book_Eum.service;

import BukkeuBukkeu.Book_Eum.domain.Book;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;
    private final BookAnalysisService bookAnalysisService;

    /**
     * ePub 파일을 업로드 -> Book 레코드를 생성 -> Book 분석을 요청 (비동기)
     * 새로 등록한 도서를 반환
     */
    @Transactional
    public Book registerBookWithEpub(String isbn,
                                     String title,
                                     String author,
                                     String plot,
                                     MultipartFile epubFile) {

        // 1. ISBN 중복 확인
        if (bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("이미 등록된 ISBN입니다 : " + isbn);
        }

        // 2. ePub 파일 업로드
        String epubUrl;
        try {
            epubUrl = fileStorageService.uploadEpub(isbn, epubFile);
        } catch (Exception e) {
            // 업로드 실패 시 DB에 아무 것도 남지 않도록 예외 던짐
            throw new RuntimeException("ePub 업로드에 실패했습니다.", e);
        }

        // 3. Book 엔티티 생성 및 저장
        Book book = Book.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .plot(plot)
                .epubFileUrl(epubUrl)
                .genres(null)               // 처음 등록시 분석된 장르 없음
                .isAnalyzed(false)          // 처음 등록 시 분석되지 않은 상태
                .analysisTriggeredBy(0L)    // 관리자의 userId : 0L
                .build();

        Book saved = bookRepository.save(book);

        // 4. 분석 요청 (비동기 구현 예정)
        bookAnalysisService.requestBookAnalysis(saved.getIsbn(), saved.getEpubFileUrl());

        return saved;
    }

    // isbn으로 도서 찾기
    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 도서를 찾을 수 없습니다 : " + isbn));
    }

    // 제목으로 도서 찾기
    @Transactional(readOnly = true)
    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    // 장르로 도서 찾기 (하나의 장르)
    @Transactional(readOnly = true)
    public List<Book> searchBooksByGenre(String genre) {
        return bookRepository.findByGenresContainingIgnoreCase(genre);
    }

    // 장르로 도서 찾기 (여러 장르 AND 조건)
    @Transactional(readOnly = true)
    public List<Book> searchBooksByAllGenres(List<String> genres) {
        return genres.stream()
                .map(bookRepository::findByGenresContainingIgnoreCase)
                .reduce((list1, list2) ->
                        list1.stream()
                                .filter(list2::contains)
                                .toList()
                )
                .orElse(List.of());
    }

    // DB에서 해당 ISBN 도서 삭제
    @Transactional
    public void deleteByIsbn(String isbn) {
        // 이 메소드에서 플레이리스트 삭제까지 같이 호출을 고려 중
        bookRepository.deleteByIsbn(isbn);
    }
}
