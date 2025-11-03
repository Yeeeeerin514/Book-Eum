package BukkeuBukkeu.Book_Eum.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// DB의 Playlist 테이블과 대응하는 엔티티

@Entity
@Getter
@Table(name = "Playlist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id; // PK

    @Column(nullable = false)
    private String isbn; // 어떤 도서의 플레이리스트인지

    @Column(nullable = false)
    private String creatorType; // 플레이리스트를 만든 주체 (AI or User)

    @Column
    private String userId; // User가 만든 경우 어떤 User가 만들었는지 (AI가 만든 경우 null)

    @OneToMany(mappedBy = "playlistID")
    private List<PlaylistAIMusic> aiMusics = new ArrayList<>();

    @Builder
    public Playlist(String isbn, String creatorType, String userId) {
        this.isbn = isbn;
        this.creatorType = creatorType;
        this.userId = userId;
    }

    // 이 플레이리스트에 AIMusic 하나를 지정한 순서로 추가
    public void addTrack(AIMusic aiMusic, int trackOrder) {
        PlaylistAIMusic link = PlaylistAIMusic.builder()
                .playlistId(this)
                .aiMusicId(aiMusic)
                .trackOrder(trackOrder)
                .build();

        this.aiMusics.add(link);
    }

    // 플레이리스트를 만든 유저가 탈퇴했을 때
    // something을 무엇으로 할지는 아직 결정 못함
    public void updateUserId(String something) {
        this.userId = something;
    }
}
