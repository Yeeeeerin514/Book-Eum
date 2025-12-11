package BukkeuBukkeu.Book_Eum.service.music;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.domain.book.Chapter;
import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import BukkeuBukkeu.Book_Eum.domain.music.Playlist;
import BukkeuBukkeu.Book_Eum.domain.music.PlaylistAIMusic;
import BukkeuBukkeu.Book_Eum.dto.music.ChapterMusic;
import BukkeuBukkeu.Book_Eum.dto.music.ChapterPlaylistResponse;
import BukkeuBukkeu.Book_Eum.dto.music.MusicTrackDto;
import BukkeuBukkeu.Book_Eum.repository.AIMusicRepository;
import BukkeuBukkeu.Book_Eum.repository.ChapterRepository;
import BukkeuBukkeu.Book_Eum.repository.PlaylistRepository;
import BukkeuBukkeu.Book_Eum.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🎵 음악 서비스
 *
 * 챕터별 플레이리스트 조회 및 음악 파일 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final PlaylistRepository playlistRepository;
    private final AIMusicRepository aiMusicRepository;
    private final ChapterRepository chapterRepository;
    private final BookService bookService;

    /**
     * 🎯 핵심 메서드: ISBN으로 챕터별 플레이리스트 조회
     *
     * @param isbn 책 ISBN
     * @return 챕터별로 구성된 전체 플레이리스트
     */
    @Transactional(readOnly = true)
    public ChapterPlaylistResponse getChapterPlaylistByIsbn(String isbn) {
        log.info("플레이리스트 조회 시작: ISBN={}", isbn);

        // 1. 책 정보 조회
        Book book = bookService.getByIsbn(isbn);

        // 2. AI 플레이리스트 조회
        Playlist playlist = playlistRepository.findAIPlaylistByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 책의 플레이리스트가 없습니다: " + isbn
                ));

        // 3. 챕터 정보 조회 (챕터 번호 순서대로)
        List<Chapter> chapters = chapterRepository.findByIsbnOrderByChapterNumAsc(isbn);

        if (chapters.isEmpty()) {
            throw new IllegalArgumentException("챕터 정보가 없습니다: " + isbn);
        }

        // 4. PlaylistAIMusic 관계를 챕터별로 그룹화
        Map<Integer, List<AIMusic>> chapterMusicMap = groupMusicByChapter(
                playlist.getAiMusics(),
                chapters.size()
        );

        // 5. 챕터별 음악 DTO 생성
        List<ChapterMusic> chapterMusicList = new ArrayList<>();
        int totalTracks = 0;

        for (Chapter chapter : chapters) {
            List<AIMusic> musicsForChapter = chapterMusicMap.getOrDefault(
                    chapter.getChapterNum(),
                    Collections.emptyList()
            );

            // AIMusic을 MusicTrackDto로 변환
            List<MusicTrackDto> tracks = musicsForChapter.stream()
                    .map(music -> MusicTrackDto.fromEntity(music, book.getTitle()))
                    .collect(Collectors.toList());

            chapterMusicList.add(ChapterMusic.builder()
                    .chapterNumber(chapter.getChapterNum())
                    .chapterTitle("Chapter " + chapter.getChapterNum())  // 챕터 제목이 있으면 사용
                    .tracks(tracks)
                    .build());

            totalTracks += tracks.size();
        }

        // 6. 응답 생성
        log.info("플레이리스트 조회 완료: ISBN={}, 챕터 수={}, 총 음악 수={}",
                isbn, chapters.size(), totalTracks);

        return ChapterPlaylistResponse.builder()
                .isbn(isbn)
                .bookTitle(book.getTitle())
                .totalChapters(chapters.size())
                .totalTracks(totalTracks)
                .chapters(chapterMusicList)
                .build();
    }

    /**
     * 플레이리스트의 음악들을 챕터별로 그룹화
     *
     * 전략: trackOrder를 기준으로 순차적으로 분배
     * 예: 총 9곡, 3개 챕터 → 각 챕터에 3곡씩
     *
     * @param playlistMusics PlaylistAIMusic 리스트
     * @param totalChapters 총 챕터 수
     * @return 챕터번호 → AIMusic 리스트 맵
     */
    private Map<Integer, List<AIMusic>> groupMusicByChapter(
            List<PlaylistAIMusic> playlistMusics,
            int totalChapters) {

        // trackOrder 순서대로 정렬
        List<PlaylistAIMusic> sortedMusics = playlistMusics.stream()
                .sorted(Comparator.comparingInt(PlaylistAIMusic::getTrackOrder))
                .collect(Collectors.toList());

        Map<Integer, List<AIMusic>> chapterMusicMap = new HashMap<>();

        // 음악을 챕터별로 균등 분배
        int musicsPerChapter = sortedMusics.size() / totalChapters;
        int remainder = sortedMusics.size() % totalChapters;

        int musicIndex = 0;
        for (int chapterNum = 1; chapterNum <= totalChapters; chapterNum++) {
            List<AIMusic> musicsForChapter = new ArrayList<>();

            // 기본 개수 + 나머지 분배
            int count = musicsPerChapter + (chapterNum <= remainder ? 1 : 0);

            for (int i = 0; i < count && musicIndex < sortedMusics.size(); i++) {
                musicsForChapter.add(sortedMusics.get(musicIndex).getAiMusic());
                musicIndex++;
            }

            chapterMusicMap.put(chapterNum, musicsForChapter);
        }

        return chapterMusicMap;
    }

    /**
     * 음악 ID로 AIMusic 엔티티 조회
     *
     * @param musicId 음악 ID
     * @return AIMusic 엔티티
     */
    @Transactional(readOnly = true)
    public AIMusic findById(Long musicId) {
        return aiMusicRepository.findById(musicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 음악을 찾을 수 없습니다: " + musicId
                ));
    }
}