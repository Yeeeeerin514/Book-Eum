package BukkeuBukkeu.Book_Eum.domain.book;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "Chapter") // DB에서 이름이 Chapter 테이블과 매핑
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ChapterId.class)
public class Chapter {

    @Id
    @Column(nullable = false)
    private String isbn; // Book의 PK를 참조하는 FK

    @Id
    @Column(name = "chapter_num", nullable = false)
    private Integer chapterNum; // 챕터 번호

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private ChapterAnalysis analysis; // LLM 분석 결과 (JSON 형식)

    @Builder
    public Chapter(String isbn, Integer chapterNum, ChapterAnalysis analysis) {
        this.isbn = isbn;
        this.chapterNum = chapterNum;
        this.analysis = analysis;
    }
}
