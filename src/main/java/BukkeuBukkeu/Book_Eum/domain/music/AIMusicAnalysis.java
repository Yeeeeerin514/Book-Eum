package BukkeuBukkeu.Book_Eum.domain.music;

// 해당 AIMusic을 만들 때 랜덤으로 선택된 요소 (LLM 분석 결과 중에서) JSON 형식

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMusicAnalysis {

    @JsonProperty("main_mood")
    private String mainMood;

    @JsonProperty("selected_genres")
    private List<String> selectedGenres;

    @JsonProperty("selected_instruments")
    private List<String> selectedInstruments;

    @JsonProperty("selected_tempo")
    private List<String> selectedTempo;

    @JsonProperty("selected_keywords")
    private List<String> selectedKeywords;
}
