package BukkeuBukkeu.Book_Eum.dto.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AIMusicGenerateResponse {

    private Long id;
    private String isbn;
    private Integer chapterNumber;
    private String audioUrl; // 클라우드에 업로드된 음악 URL
}
