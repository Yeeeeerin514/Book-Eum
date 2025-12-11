package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.music.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 🎵 Playlist Repository
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /**
     * ISBN으로 플레이리스트 조회
     *
     * @param isbn 책 ISBN
     * @return Playlist
     */
    Optional<Playlist> findByIsbn(String isbn);

    Optional<Playlist> findByIsbnAndChapterNumAndCreatorType(
            String isbn,
            Integer chapterNum,
            String creatorType
    );

    List<Playlist> findByIsbnAndCreatorTypeOrderByChapterNumAsc(
            String isbn,
            String creatorType
    );

    /**
     * ISBN으로 플레이리스트 존재 여부 확인
     */
    boolean existsByIsbn(String isbn);
}