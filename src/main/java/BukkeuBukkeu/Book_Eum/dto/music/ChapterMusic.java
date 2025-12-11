package BukkeuBukkeu.Book_Eum.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 🎵 챕터별 음악 목록 DTO
 *
 * 각 챕터마다 3~5곡의 음악 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterMusic {

    private Integer chapterNumber;    // 챕터 번호
    private String chapterTitle;      // 챕터 제목
    private List<MusicTrackDto> tracks;  // 음악 트랙 리스트
}