package BukkeuBukkeu.Book_Eum.domain.system;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity // 테이블과 매핑되는 JPA 엔티티
@Getter // getter 자동 생성
@Table(name = "Reading_Log") // DB에서 이름이 ReadingLog인 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 생성
public class ReadingLog {

    @Id // PK 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY) // FK to User
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 어떤 유저의 이력인지

    @ManyToOne(fetch = FetchType.LAZY) // FK to Book
    @JoinColumn(name = "isbn", nullable = false)
    private Book book; // 어떤 책인지

    @Column(nullable = false, name = "last_open_date")
    private LocalDateTime lastOpenDate; // 마지막으로 책을 연 시각

    @Column(nullable = false, name = "last_chapter")
    private Integer lastChapter; // 마지막으로 읽은 챕터 번호

    @Column(nullable = false, name = "last_point")
    private String lastPoint; // 마지막 읽은 위치 (타입 논의 필요)

    @Column(nullable = false, name = "is_deleted")
    private Boolean isDeleted = false;  // 이력 숨김/삭제 여부

    @Builder
    public ReadingLog(User user, Book book, Integer lastChapter, String lastPoint) {
        this.user = user;
        this.book = book;
        this.lastOpenDate = LocalDateTime.now();
        this.lastChapter = lastChapter;
        this.lastPoint = lastPoint;
        this.isDeleted = false;
    }

    // 이력 업데이트
    public void updateReadingLog(Integer lastChapter, String lastPoint) {
        this.lastOpenDate = LocalDateTime.now();
        this.lastChapter = lastChapter;
        this.lastPoint = lastPoint;
    }

    // 이력 삭제 (soft delete)
    public void deleteReadingLog() {
        this.isDeleted = true;
    }
}
