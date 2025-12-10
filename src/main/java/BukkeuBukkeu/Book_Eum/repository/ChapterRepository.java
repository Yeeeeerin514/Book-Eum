package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.book.Chapter;
import BukkeuBukkeu.Book_Eum.domain.book.ChapterId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, ChapterId> {

    // 특정 도서의 모든 챕터 조회
    List<Chapter> findByIsbn(String isbn);

    // 특정 도서의 특정 챕터 하나 조회
    Optional<Chapter> findByIsbnAndChapterNum(String isbn, Integer chapterNum);

    // 새로 분석할 때 기존 분석 제거 (아마 안 쓸듯)
    void deleteByIsbn(String isbn);
}
