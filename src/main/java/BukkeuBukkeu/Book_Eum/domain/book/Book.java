package BukkeuBukkeu.Book_Eum.domain.book;

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

    @Column(nullable = false, length = 500, name = "epub_file_url")
    private String epubFileUrl;

    @Column(nullable = false, name = "is_analyzed")
    private Boolean isAnalyzed;

    @Builder
    public Book(String isbn,
                String title,
                String author,
                String plot,
                String epubFileUrl,
                boolean isAnalyzed) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.plot = plot;
        this.epubFileUrl = epubFileUrl;
        this.isAnalyzed = isAnalyzed;
    }

    // 분석 결과 업데이트
    public void markAsAnalyzed(boolean analyzed){
        this.isAnalyzed = analyzed;
    }
}