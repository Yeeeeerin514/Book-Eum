package BukkeuBukkeu.Book_Eum.dto.book;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

// 서버 -> AI
// 도서 내용 분석을 요청할 때 전달할 객체

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BookAnalyzeRequest {

    private String isbn;
    private String fileUrl; // epub 파일 경로
}
