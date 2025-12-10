package BukkeuBukkeu.Book_Eum.domain.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DB의 Book 테이블과 대응하는 엔티티

@Entity // 테이블과 매핑되는 JPA 엔티티
@Getter // getter 자동 생성
@Table(name = "Book") // DB에서 이름이 Book인 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 생성
public class Book {

    @Id // PK 지정
    @Column(length = 20)
    private String isbn; // 도서 id (따로 id를 만들지 않고, isbn으로 관리)

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 500)
    private String plot;

    @Column(nullable = false, length = 500, name = "epub_file_url")
    private String epubFilePath;

    @Column(name = "total_chapters")
    private Integer totalChapters;

    @Enumerated(EnumType.STRING) // DB에 문자열로 저장
    @Column(nullable = false, name = "analysis_status")
    private Status status = Status.NOT_REQUESTED;

    @Builder
    public Book(String isbn,
                String title,
                String author,
                String plot,
                String epubFilePath) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.plot = plot;
        this.epubFilePath = epubFilePath;
    }

    // 총 챕터 개수 설정
    public void updateTotalChapters(Integer totalChapters) {
        this.totalChapters = totalChapters;
    }

    // 내용분석 진행중
    public void markAnalysisProcessing() {
        this.status = Status.ANALYSIS_PROCESSING;
    }

    // 내용분석 완료
    public void markAnalysisCompleted() {
        this.status = Status.ANALYSIS_COMPLETED;
    }

    // 내용분석 실패
    public void markAnalysisFailed() {
        this.status = Status.ANALYSIS_FAILED;
    }

    // 음악생성 진행중
    public void markGenerationProcessing() {
        this.status = Status.GENERATION_PROCESSING;
    }

    // 음악생성 실패
    public void markGenerationFailed() {
        this.status = Status.GENERATION_FAILED;
    }

    // 내용분석 & 음악생성 완료
    public void markCompleted() {
        this.status = Status.COMPLETED;
    }
}