package BukkeuBukkeu.Book_Eum.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 도서 분석 결과를 클라이언트에게

@Getter
@AllArgsConstructor
public class BookAnalyzeResponse {

    private String isbn;
    private boolean isAnalyzed;
    private String genre;
    //private String emotion;
    private String prompt;
}
