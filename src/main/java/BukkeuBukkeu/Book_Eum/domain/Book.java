package BukkeuBukkeu.Book_Eum.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB의 Book 테이블과 대응하는 엔티티

@Entity // 테이블과 매핑되는 JPA 엔티티
@Getter // getter 자동 생성
@Table(name = "Book") // DB에서 이름이 User인 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 생성
public class Book {

    @Id // PK 지정
    private String isbn; // 도서 id (따로 id를 만들지 않고, isbn으로 관리)

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 500)
    private String plot;

    @Column(nullable = false)
    private String epubFileUrl;

    @Column
    private String genres;

    @Column(nullable = false)
    private Boolean isAnalyzed;

    @Column
    private Long analysisTriggeredBy; // 모두 관리자가 트리거 할텐데, 이 필드가 존재하는 이유가 뭐임?

    @Builder
    public Book(String isbn,
                String title,
                String author,
                String plot,
                String epubFileUrl,
                String genres,
                boolean isAnalyzed,
                Long analysisTriggeredBy) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.plot = plot;
        this.epubFileUrl = epubFileUrl;
        this.genres = genres;
        this.isAnalyzed = isAnalyzed;
        this.analysisTriggeredBy = analysisTriggeredBy;
    }

    // 분석 결과 업데이트
    public void markAsAnalyzed(boolean analyzed){
        this.isAnalyzed = analyzed;
    }

    // 도서 장르 키워드 업데이트
    public void updateGenres(String genres){
        this.genres = genres;
    }
}
