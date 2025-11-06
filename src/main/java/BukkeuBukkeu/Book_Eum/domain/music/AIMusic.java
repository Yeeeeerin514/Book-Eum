package BukkeuBukkeu.Book_Eum.domain.music;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// DB의 AIMusic에 대응하는 엔티티

@Entity // 테이블과 매핑되는 JPA 엔티티
@Getter // getter 자동 생성
@Table(name = "AI_Music") // DB에서 이름이 User인 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 생성
@AllArgsConstructor
public class AIMusic {

    @Id // PK 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "audio_file_url", length = 500)
    private String audioFileUrl;

    @Column(nullable = false, name = "skip_cnt")
    private int skipCnt;

    @OneToMany(mappedBy = "aiMusic")
    private List<PlaylistAIMusic> playlists = new ArrayList<>();

    @Builder
    public AIMusic(String title, String audioFileUrl, int skipCnt){
        this.title = title;
        this.audioFileUrl = audioFileUrl;
        this.skipCnt = skipCnt;
    }

    // 유저가 음악을 스킵
    public void increaseSkipCnt(){ this.skipCnt += 1; }
}