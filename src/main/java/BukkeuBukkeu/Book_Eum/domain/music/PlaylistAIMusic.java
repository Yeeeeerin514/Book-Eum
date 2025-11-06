package BukkeuBukkeu.Book_Eum.domain.music;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB의 PlaylistAIMusic 테이블과 대응하는 엔티티 (다대다 해소 엔티티)

@Entity
@Getter
@Table(name = "Playlist_AI_Music")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(PlaylistAIMusicId.class)
public class PlaylistAIMusic {

    @Id
    @Column(name = "playlist_id", nullable = false)
    private Long playlistId; // Playlist : PlaylistAIMusic이 N:1 관계 (어떤 플레이리스트에 속하는지)

    @Id
    @Column(name = "ai_music_id", nullable = false)
    private Long aiMusicId; // AIMusic : PlaylistAIMusic이 N:1 관계 (어떤 AI 생성 음악과 연결되는지)

    @ManyToOne(fetch = FetchType.LAZY) // Playlist와 N:1 관계를 JPA 레벨에서 표현
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY) // AIMusic과 N:1 관계를 JPA 레벨에서 표현
    @JoinColumn(name = "ai_music_id", insertable = false, updatable = false)
    private AIMusic aiMusic;

    @Column(nullable = false, name = "track_order")
    private int trackOrder; // 재생 순서

    // 생성자 기능 + 특정 플레이리스트에 특정 음악을 추가
    public PlaylistAIMusic(Playlist playlist, AIMusic aiMusic, int trackOrder) {
        this.playlist = playlist;
        this.aiMusic = aiMusic;
        this.trackOrder = trackOrder;
        this.playlistId = playlist.getId();
        this.aiMusicId = aiMusic.getId();
        playlist.getAiMusics().add(this); // Playlist 엔티티에 현재 관계 추가
        aiMusic.getPlaylists().add(this); // AIMusic 엔티티에 현재 관계 추가
    }

    // 플레이리스트 내에서 음악 순서 업데이트
    public void updateTrackOrder(int newOrder) {
        this.trackOrder = newOrder;
    }
}