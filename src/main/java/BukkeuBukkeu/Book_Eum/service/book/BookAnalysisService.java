package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.Book;
import BukkeuBukkeu.Book_Eum.external.FastApiAnalysis;
import BukkeuBukkeu.Book_Eum.dto.book.AnalysisResult;
import BukkeuBukkeu.Book_Eum.dto.book.BookAnalyzeResponse;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;

// 도서 있는지 조회 → 외부(AI)에 분석 요청 → 도서 엔티티 업데이트

@Service
@RequiredArgsConstructor
public class BookAnalysisService {

    private final BookRepository bookRepository;
    private final FastApiAnalysis fastApiAnalysis;

    // 로컬 EPUB 파일들이 모여있는 디렉토리 (중간발표용)
    @Value("${analysis.local-epub-dir:/Users/luna/CAU/2025-2/Capston Design 1/epub 파일}")
    private String localEpubDir;

    @Transactional
    public BookAnalyzeResponse analyzeBook(String isbn) {

        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));

        // 1. 로컬 EPUB 경로 결정
        String epubPath;
        if (book.getEpubFileUrl() != null && !book.getEpubFileUrl().isBlank()) {
            // 신규 도서 등록 시에 이미 저장해 둔 경우
            epubPath = book.getEpubFileUrl();
        } else {
            // 간단 버전: 디렉토리 + isbn.epub 규칙
            epubPath = Paths.get(localEpubDir, isbn + ".epub").toString();
        }

        // 2. FastAPI 서버 호출
        AnalysisResult result = fastApiAnalysis.analyze(epubPath);

        // 3. 분석 결과를 Book 엔티티에 반영
        book.markAsAnalyzed(true);

        // genres/emotions는 배열이므로 단순히 join해서 String으로 저장
        if (result.getEmotions() != null) { // 일단, 중간 발표는 단일 챕터 도서이므로 emotion = genre라고 생각
            book.updateGenres(String.join(", ", result.getGenres()));
        }

        // emotion 등 다른 분석 결과 어디에 저장...?

        // Book JPA 엔티티는 @Transactional 안에서 dirty checking으로 자동 업데이트됨
        // Response DTO 생성
        return new BookAnalyzeResponse(
                book.getIsbn(),
                book.getIsAnalyzed() != null && book.getIsAnalyzed(),
                book.getGenres(),
                result.getPrompt()
        );
    }
}
