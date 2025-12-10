package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.dto.book.BookAnalyzeRequest;
import BukkeuBukkeu.Book_Eum.dto.book.BookAnalyzeResponse;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

// 도서 신규 등록 후 -> AI에 분석 요청
// AI의 분석 응답을 기다리지 않고 비동기 실행

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAnalysisService {

    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.analysis.request:/books/analyze}")
    private String analyzePath;

    /**
     * Book 등록이 끝난 뒤 비동기로 호출되는 메서드
     * - AI 서버에 "책 한 권 분석 시작해줘" 요청만 보냄
     * - AI는 이후에 챕터별 분석 결과를 콜백 API로 보내줌
     */
    @Async
    @Transactional
    public void requestAnalysisAsync(Book book) {

        String isbn = book.getIsbn();

        // 1. 상태를 ANALYSIS_PROCESSING 으로 변경
        book.markAnalysisProcessing();
        // book 이 영속 상태이면 별도 save() 필요 없음. (아니면 bookRepository.save(book))

        // 2. 요청 DTO 구성
        BookAnalyzeRequest request = new BookAnalyzeRequest(
                isbn,
                book.getEpubFilePath()  // 로컬 파일 경로
        );

        String url = aiBaseUrl + analyzePath;
        log.info("[BookAnalysis] AI 분석 요청 url={} isbn={}", url, isbn);

        try {
            BookAnalyzeResponse meta = restTemplate.postForObject(
                    url,
                    request,
                    BookAnalyzeResponse.class
            );
            log.info("[BookAnalysis] AI 분석 요청 성공 isbn={}", isbn);

            if (meta != null) {
                responseAnalysisCallback(meta);
            } else {
                log.warn("[BookAnalysis] 메타데이터 응답이 null 입니다. isbn={}", isbn);
            }

        } catch (Exception e) {
            log.error("[BookAnalysis] AI 분석 요청 실패 isbn={}", isbn, e);
            // 실패 시 상태를 실패로 바꾸고 싶다면:
            book.markAnalysisFailed();
        }
    }

    /**
     * AI 서버가 모든 챕터 분석한 후 메타데이터를 콜백하는 메서드
     * - /books/analyze 엔드포인트에서 호출
     */
    @Transactional
    public void responseAnalysisCallback(BookAnalyzeResponse metadata) {

        String isbn = metadata.getIsbn();
        Integer totalChapters = metadata.getTotalChapters();
        String statusFromAi = metadata.getStatus();

        log.info("[BookAnalysis] 메타데이터 콜백 수신 isbn={} status={} totalChapters={}",
                isbn, statusFromAi, totalChapters);

        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException(
                        "메타데이터 콜백: 존재하지 않는 ISBN 입니다. isbn=" + isbn));

        // 1) totalChapters 업데이트
        if (totalChapters != null) {
            book.updateTotalChapters(totalChapters);
        }

        // 2) AI status 값에 따라 Book.status 변경
        if (statusFromAi != null) {

            if (statusFromAi.equals("success")) {
                book.markAnalysisCompleted();
            } else if (statusFromAi.equals("fail")) {
                book.markAnalysisFailed();
            } else {
                log.warn("[BookAnalysis] 알 수 없는 status 값={} isbn={}", statusFromAi, isbn);
            }
        }

        // message 는 일단 로그용으로만 사용
        if (metadata.getMessage() != null) {
            log.info("[BookAnalysis] 메타데이터 message isbn={} message={}",
                    isbn, metadata.getMessage());
        }
    }
}
