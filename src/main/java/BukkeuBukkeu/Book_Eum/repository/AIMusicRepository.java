package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AIMusicRepository extends JpaRepository<AIMusic, Long> {

    // 클라우드 링크로 조회
    Optional<AIMusic> findByAudioFilePath(String audioFilePath);

    // 특정 mood를 기준으로 조회
    //List<AIMusic> findByMoodsContainingIgnoreCase(String mood);

    // 재생 스킵 횟수(skipCnt) 기준 조회
    List<AIMusic> findBySkipCntGreaterThanEqual(int skipCount);

    // AI 음악 삭제 : JPA가 제공하는 delete() 사용
}
