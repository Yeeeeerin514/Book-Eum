package BukkeuBukkeu.Book_Eum.dto.music;

// 책 한 권에 대한 북플리

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookPlaylistResponse {

    private String isbn;
    private String title;
    private Integer totalChapters;
    private Integer totalMusic;
    private List<ChapterPlaylistResponse> chapterPlaylist;
}
