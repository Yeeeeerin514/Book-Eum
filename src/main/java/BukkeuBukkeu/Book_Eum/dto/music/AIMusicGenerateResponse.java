package BukkeuBukkeu.Book_Eum.dto.music;

import BukkeuBukkeu.Book_Eum.domain.music.AIMusicAnalysis;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

// AI -> 서버
// 음악 생성 이후 AI로부터 전달받을 객체
// AI와 통신하는 "진짜 응답"

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AIMusicGenerateResponse {

    // HTTP Header
    private String isbn;
    private int chapterNumber;

    private String mainMood;
    private List<String> selectedGenres;
    private List<String> selectedInstruments;
    private List<String> selectedTempo;
    private List<String> selectedKeywords;

    private String finalPrompt;
    private Double generationTimeSec;

    // HTTP Body
    private String filename;        // ISBN_Chapter.wav
    private String audioFilePath;   // 로컬에 저장된 실제 경로

    /**
     * DB에 저장할 AIMusicAnalysis로 변환하는 헬퍼 메서드
     */
    public AIMusicAnalysis toAnalysis() {
        return new AIMusicAnalysis(
                mainMood,
                selectedGenres,
                selectedInstruments,
                selectedTempo,
                selectedKeywords
        );
    }
}
