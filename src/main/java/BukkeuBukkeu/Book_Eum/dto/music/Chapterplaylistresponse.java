package BukkeuBukkeu.Book_Eum.dto.music;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 🎵 챕터별 플레이리스트 응답 DTO
 *
 * 프론트엔드 요청: GET /music/playlist/{isbn}
 *
 * 전체 플레이리스트를 챕터별로 구성하여 반환
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterPlaylistResponse {

    private String isbn;              // 책 ISBN
    private String bookTitle;         // 책 제목
    private Integer totalChapters;    // 총 챕터 수
    private Integer totalTracks;      // 총 음악 수
    private List<ChapterMusic> chapters;  // 챕터별 음악 목록
}