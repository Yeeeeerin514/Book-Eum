package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.music.Playlist;
import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import BukkeuBukkeu.Book_Eum.domain.music.PlaylistAIMusic;
import BukkeuBukkeu.Book_Eum.domain.music.PlaylistAIMusicId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface PlaylistAIMusicRepository extends JpaRepository<PlaylistAIMusic, PlaylistAIMusicId> {

    // 특정 플레이리스트의 모든 음악 조회 (trackOrder 순서대로)
    List<PlaylistAIMusic> findByPlaylistIdOrderByTrackOrderAsc(Playlist playlistId);

    // 특정 플레이리스트에서 trackOrder에 해당하는 AI 음악 조회
    Optional<PlaylistAIMusic> findByPlaylistIdAndTrackOrder(Playlist playlistID, int trackOrder);

    // 특정 플레이리스트에 특정 AI 음악이 이미 있는지 확인
    boolean existsByPlaylistIdAndAiMusicId(Playlist playlistId, AIMusic aiMusicId);

    // 특정 trackOrder가 이미 사용 중인지 확인
    boolean existsByPlaylistIdAndTrackOrder(Playlist playlistId, int trackOrder);

    // 특정 AI 음악이 어떤 플레이리스트들에 포함되어 있는지 조회
    List<PlaylistAIMusic> findByAiMusicId(AIMusic aiMusicId);

    // 특정 플레이리스트에서 특정 AI 음악을 삭제
    void deleteByPlaylistIdAndAiMusicId(Playlist playlistId, AIMusic aiMusicId);

    // 특정 플레이리스트에 있는 AI 음악 전체를 삭제 (플레이리스트 삭제 시 필요)
    void deleteByPlaylistId(Playlist playlistID);
}
