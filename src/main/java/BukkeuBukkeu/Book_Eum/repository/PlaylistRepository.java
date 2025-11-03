package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // 특정 도서(isbn 기준)에 해당하는 모든 플레이리스트 조회
    List<Playlist> findByIsbn(String isbn);
    Page<Playlist> findByIsbn(String isbn, Pageable pageable);

    // 도서 삭제 시 연쇄적으로 플레이리스트도 삭제
    void deleteByIsbn(String isbn);
}
