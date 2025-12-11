package BukkeuBukkeu.Book_Eum.dto.music;

import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 🎵 음악 트랙 상세 정보 DTO
 *
 * 개별 음악 파일의 메타데이터와 다운로드 URL 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicTrackDto {

    private String id;                // 음악 ID (예: "music_001")
    private String title;             // 곡 제목
    private String artist;            // 아티스트 (기본값: "AI Generated")
    private String album;             // 앨범명 (책 제목)
    private Long duration;            // 재생 시간 (밀리초) - 선택적
    private String downloadUrl;       // 다운로드 URL (예: "/music/1/download")
    private String albumArtUrl;       // 앨범 아트 URL (선택적)

    /**
     * AIMusic 엔티티를 DTO로 변환
     */
    public static MusicTrackDto fromEntity(AIMusic aiMusic, String bookTitle) {
        return MusicTrackDto.builder()
                .id(String.valueOf(aiMusic.getId()))
                .title(aiMusic.getTitle())
                .artist("AI Generated")  // AI 생성 음악
                .album(bookTitle)
                .duration(null)  // duration 정보가 있으면 추가
                .downloadUrl("/music/" + aiMusic.getId() + "/download")
                .albumArtUrl(null)  // 앨범 아트가 있으면 추가
                .build();
    }
}