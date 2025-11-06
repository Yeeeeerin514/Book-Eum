package BukkeuBukkeu.Book_Eum.external;

import BukkeuBukkeu.Book_Eum.dto.book.AnalysisResult;
import BukkeuBukkeu.Book_Eum.dto.book.FileRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FastApiAnalysis {

    private final RestTemplate restTemplate;
    private final String fastApiUrl;

    public FastApiAnalysis(
            @Value("${analysis.fastapi.url:http://localhost:8000/analyze}") String fastApiUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.fastApiUrl = fastApiUrl;
    }

    public AnalysisResult analyze(String localFilePath) {
        FileRequest body = new FileRequest(localFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FileRequest> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<AnalysisResult> response =
                restTemplate.postForEntity(fastApiUrl, requestEntity, AnalysisResult.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to call FastAPI analysis server");
        }

        return response.getBody();
    }
}
