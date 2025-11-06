package BukkeuBukkeu.Book_Eum.dto.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// AI에서 반환하는 분석 결과 (중간 발표 이전)

@Getter
@Setter
@NoArgsConstructor
public class AnalysisResult {

    // 모두 JSON 키와 동일하게 하든가 어노테이션 쓰든가
    private String mainMood;
    private String[] emotions;
    private String[] genres;
    private String[] instruments;
    private String[] tempo;
    private String[] keywords;
    private String prompt;
}
