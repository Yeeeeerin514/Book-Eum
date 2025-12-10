package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.dto.book.BookRegisterRequest;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import BukkeuBukkeu.Book_Eum.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// 클라이언트가 [신규 도서 등록]을 서버에게 요청
// DB에 도서가 등록되면, 도서 분석 서비스를 비동기적으로 호출하고, 클라이언트에게는 정상 등록 메세지 보냄

@Slf4j
@Service
@RequiredArgsConstructor
public class BookRegisterService {

    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;
    private final BookAnalysisService bookAnalysisService;

    /**
     * ePub 파일을 업로드 -> Book 레코드를 생성 -> Book 분석을 "비동기"로 요청
     * 새로 등록한 도서를 반환
     */
    @Transactional
    public Book register(BookRegisterRequest request) {
        String isbn = request.getIsbn();
        String title = request.getTitle();
        String author = request.getAuthor();
        String plot = request.getPlot();
        MultipartFile epubFile = request.getEpubFile();

        // 1. ISBN 중복 체크
        if (bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("이미 등록된 ISBN 입니다: " + isbn);
        }

        // 2. EPUB 파일 로컬 저장
        String epubUrl = fileStorageService.storeEpub(epubFile, isbn);

        // 3. Book 엔티티 생성 및 저장
        Book book = Book.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .plot(plot)
                .epubFilePath(epubUrl)   // DB의 epub_file_url 컬럼에 들어감
                .build();

        Book saved = bookRepository.save(book);

        // 4. 분석 요청 (비동기)
        try {
            bookAnalysisService.requestAnalysisAsync(saved);
        } catch (Exception e) {
            // @Async 메서드에서 예외가 바로 터질 일은 거의 없지만,
            // 혹시 프록시 설정 문제 등으로 동기호출이 되어도 등록은 깨지지 않게 방어
            log.error("[BookRegister] 비동기 분석 요청 중 예외 발생 isbn={}", saved.getIsbn(), e);
        }

        // 등록 자체는 정상 완료
        return saved;
    }
}
