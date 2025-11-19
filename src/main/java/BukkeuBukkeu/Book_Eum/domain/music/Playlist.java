package BukkeuBukkeu.Book_Eum.domain.music;

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
    private String isbn; // FK : 어떤 도서의 플레이리스트인지

    @Column(nullable = false, name = "creator_type")
    private String creatorType; // 플레이리스트를 만든 주체 (AI or User)

    @Column(name = "user_id")
    private String userId; // User가 만든 경우 어떤 User가 만들었는지 (AI가 만든 경우 어떤 값을 넣을지 논의 필요)

    @OneToMany(mappedBy = "playlist") // PlaylistAIMusic과 1:N 관계를 JPA 레벨에서 표현
    private List<PlaylistAIMusic> aiMusics = new ArrayList<>(); // 이 플레이리스트에 어떤 음악이 있는지

    // ExistingMusic과의 1:N 관계 논의 필요

    @Builder
    public Playlist(String isbn, String creatorType, String userId) {
        this.isbn = isbn;
        this.creatorType = creatorType;
        this.userId = userId;
    }

    // 이 플레이리스트에 AIMusic 하나를 지정한 순서로 추가
    public void addAiMusicToPlaylist(AIMusic aiMusic, int trackOrder) {
        new PlaylistAIMusic(this, aiMusic, trackOrder);
    }

    // 이 플레이리스트에서 AIMusic 하나를 제거 메소드 추가 예정

    // ExistingMusic 하나를 지정한 순서로 추가하는 메소드 추가 예정

    // 이 플레이리스트에서 ExistingMusic 하나를 제거 메소드 추가 예정

    // 플레이리스트를 만든 유저가 탈퇴했을 때 플레이리스트는 유지하고자 하는데, 이때 userId 필드 값을 무엇으로 할 지에 대한 논의 필요
    public void updateUserId(String deletedId) {
        this.userId = deletedId;
    }
}
