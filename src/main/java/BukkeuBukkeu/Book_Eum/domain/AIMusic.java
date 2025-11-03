package BukkeuBukkeu.Book_Eum.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB의 AIMusic에 대응하는 엔티티

@Entity // 테이블과 매핑되는 JPA 엔티티
@Getter // getter 자동 생성
@Table(name = "AIMusic") // DB에서 이름이 User인 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 생성
public class AIMusic {

    @Id // PK 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String audioFileUrl;

    @Column(nullable = false)
    private String moods;

    @Column(nullable = false)
    private int skipCnt;

    @Builder
    public AIMusic(String title, String audioFileUrl, String moods, int skipCnt){
        this.title = title;
        this.audioFileUrl = audioFileUrl;
        this.moods = moods;
        this.skipCnt = skipCnt;
    }

    // 유저가 음악을 스킵
    public void increaseSkipCnt(){
        this.skipCnt += 1;
    }

    // AIMusic 분위기 업데이트
    public void updateMoods(String moods){
        this.moods = moods;
    }
}