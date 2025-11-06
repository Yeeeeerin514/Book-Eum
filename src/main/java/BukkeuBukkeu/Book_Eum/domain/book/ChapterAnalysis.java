package BukkeuBukkeu.Book_Eum.domain.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// LLM 분석 결과 JSON 형식

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterAnalysis {

    @JsonProperty("main_mood")
    private String mainMood;

    private List<String> emotions;
    private List<String> genres;
    private List<String> instruments;
    private List<String> tempo;
    private List<String> keywords;
}
