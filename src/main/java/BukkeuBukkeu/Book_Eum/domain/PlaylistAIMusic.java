package BukkeuBukkeu.Book_Eum.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB의 PlaylistAIMusic 테이블과 대응하는 엔티티

@Entity
@Getter
@Table(name = "PlaylistAIMusic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(PlaylistAIMusicId.class)
public class PlaylistAIMusic {

    @MapsId("playlistId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlistId", nullable = false)
    private Playlist playlistId; // Playlist : PlaylistAIMusic이 N:1 관계 (어떤 플레이리스트에 속하는지)

    @MapsId("aiMusicId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aiMusicId", nullable = false)
    private AIMusic aiMusicId; // AIMusic : PlaylistAIMusic이 N:1 관계 (어떤 AI 생성 음악과 연결되는지)

    @Column(nullable = false, unique = true)
    private int trackOrder; // 재생 순서

    @Builder
    public PlaylistAIMusic(Playlist playlistId, AIMusic aiMusicId, int trackOrder) {
        this.playlistId = playlistId;
        this.aiMusicId = aiMusicId;
        this.trackOrder = trackOrder;
    }

    // 플레이리스트 내에서 음악 순서 업데이트
    public void updateTrackOrder(int newOrder) {
        this.trackOrder = newOrder;
    }
}