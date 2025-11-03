package BukkeuBukkeu.Book_Eum.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// PlaylistAIMusic의 복합 PK를 위한 엔티티

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PlaylistAIMusicId implements Serializable {

    private Long playlistId; // Playlist 테이블의 PK

    private Long aiMusicId; // AIMusic 테이블의 PK
}
