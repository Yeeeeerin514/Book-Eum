package BukkeuBukkeu.Book_Eum.dto.book;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

// AI -> 서버
// 모든 챕터를 분석한 후에, 책 분석 메타데이터 객체

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BookAnalyzeResponse {

    private String status;
    private String message; // 분석 결과 메세지
    private String isbn; // 분석한 책 isbn
    private int totalChapters; // 이 도서의 총 챕터 개수
}
