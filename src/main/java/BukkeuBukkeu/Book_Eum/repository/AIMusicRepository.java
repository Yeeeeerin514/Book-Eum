package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 🎵 AIMusic Repository
 */
@Repository
public interface AIMusicRepository extends JpaRepository<AIMusic, Long> {

    /**
     * ID로 음악 조회
     */
    Optional<AIMusic> findById(Long id);
}