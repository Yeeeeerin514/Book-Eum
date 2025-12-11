package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.book.Chapter;
import BukkeuBukkeu.Book_Eum.domain.book.ChapterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 📚 Chapter Repository
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, ChapterId> {

    /**
     * ISBN으로 모든 챕터 조회 (챕터 번호 순서대로)
     */
    List<Chapter> findByIsbnOrderByChapterNumAsc(String isbn);
}