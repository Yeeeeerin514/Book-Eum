package BukkeuBukkeu.Book_Eum.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// 챕터 하나에 대한 플리

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterPlaylistResponse {

    private Integer chapterNum;
    private List<MusicResponse> musics;
}