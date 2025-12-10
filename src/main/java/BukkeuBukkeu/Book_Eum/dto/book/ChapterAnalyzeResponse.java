package BukkeuBukkeu.Book_Eum.dto.book;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

// AI -> 서버
// 도서 내용 분석 결과를 한 챕터마다 받을 때 객체

@Data
@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChapterAnalyzeResponse {

    private String isbn;
    private int chapterNumber;
    private String mainMood;

    private List<String> emotions;
    private List<String> genres;
    private List<String> instruments;
    private List<String> tempo;
    private List<String> keywords;
}
