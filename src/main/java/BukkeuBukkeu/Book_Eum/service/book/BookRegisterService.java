package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.Book;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import BukkeuBukkeu.Book_Eum.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// 신규 도서 등록

@Service
@RequiredArgsConstructor
public class BookRegisterService {

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
        bookAnalysisService.analyzeBook(saved.getIsbn());

        return saved;
    }
}
