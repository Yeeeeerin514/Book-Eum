package BukkeuBukkeu.Book_Eum.domain.user;

import jakarta.persistence.*;
import lombok.*;

// DB의 User 테이블과 대응하는 엔티티

@Entity // 테이블과 매핑되는 JPA 엔티티
@Getter // getter 자동 생성
@Table(name = "Users") // DB에서 이름이 User인 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 생성
public class User {

    @Id // PK 지정
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long userId; // 시스템 상에서 사용되는 유저 고유 id

    @Column(nullable = false, unique = true)
    private String id; // 유저가 로그인 할 때 사용하는 id

    @Column(nullable = false, name = "pass_word")
    private String password; // 유저 비밀번호 (암호화 예정)

    @Column(nullable = false, name = "user_name")
    private String name; // 유저 이름

    @Column
    private String spotifyAccount; // 연동된 스포티파이 계정

    @Builder
    public User(String id, String password, String name){
        this.id = id;
        this.password = password;
        this.name = name;
    }

    // 스포티파이 계정 연동
    public void linkSpotifyAccount(String spotifyAccount){
        this.spotifyAccount = spotifyAccount;
    }

    // 스포티파이 계정 연동 해제
    public void unlinkSpotifyAccount(){
        this.spotifyAccount = null;
    }

    // 이름 수정
    public void updateName(String newName){
        this.name = newName;
    }
}