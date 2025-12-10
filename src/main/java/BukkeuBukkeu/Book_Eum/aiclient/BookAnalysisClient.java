package BukkeuBukkeu.Book_Eum.aiclient;

import BukkeuBukkeu.Book_Eum.dto.book.BookAnalyzeRequest;
import BukkeuBukkeu.Book_Eum.dto.book.ChapterAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookAnalysisClient {

    private final RestTemplate restTemplate;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.analysis.request}")
    private String analyzePath;

    public List<ChapterAnalyzeResponse> analyzeBook(BookAnalyzeRequest request) {

        String url = baseUrl + analyzePath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<BookAnalyzeRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ChapterAnalyzeResponse[]> response =
                restTemplate.postForEntity(url, entity, ChapterAnalyzeResponse[].class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("AI 분석 서버 호출 실패: " + response.getStatusCode());
        }

        ChapterAnalyzeResponse[] body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("AI 분석 서버 응답이 비어 있습니다.");
        }

        return Arrays.asList(Objects.requireNonNull(body));
    }
}
