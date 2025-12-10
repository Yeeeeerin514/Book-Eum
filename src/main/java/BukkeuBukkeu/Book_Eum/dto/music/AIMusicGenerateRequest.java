package BukkeuBukkeu.Book_Eum.dto.music;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

// 서버 -> AI
// 챕터 하나에 대해 음악 생성을 요청할 때 DTO

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AIMusicGenerateRequest {

    private String isbn;
    private int chapterNumber;
    private String mainMood;

    private List<String> emotions;
    private List<String> genres;
    private List<String> instruments;
    private List<String> tempo;
    private List<String> keywords;
}
