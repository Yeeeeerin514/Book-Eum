package BukkeuBukkeu.Book_Eum.service.music;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.domain.book.Chapter;
import BukkeuBukkeu.Book_Eum.domain.book.ChapterAnalysis;
import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import BukkeuBukkeu.Book_Eum.domain.music.AIMusicAnalysis;
import BukkeuBukkeu.Book_Eum.domain.music.Playlist;
import BukkeuBukkeu.Book_Eum.domain.music.PlaylistAIMusic;
import BukkeuBukkeu.Book_Eum.dto.music.BookPlaylistResponse;
import BukkeuBukkeu.Book_Eum.dto.music.ChapterPlaylistResponse;
import BukkeuBukkeu.Book_Eum.dto.music.MusicResponse;
import BukkeuBukkeu.Book_Eum.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    // 챕터당 몇 곡 줄지
    private static final int MUSIC_PER_CHAPTER = 5;

    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;
    private final AIMusicRepository aiMusicRepository;

    /**
     * /music/playlist/{isbn}
     *
     * - ❗ DB에 Playlist/PlaylistAIMusic 을 저장하지 않는다.
     * - 요청이 들어올 때마다, ChapterAnalysis ↔ AIMusicAnalysis 유사도 기반으로
     *   각 챕터의 챕터플레이리스트를 "그때그때" 계산해서 DTO만 만들어서 반환한다.
     */
    public BookPlaylistResponse buildBookPlaylist(String isbn) {

        // 1. 도서 조회
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book not found: " + isbn));

        // 2. 챕터 목록 조회
        List<Chapter> chapters =
                chapterRepository.findByIsbnOrderByChapterNumAsc(isbn);

        if (chapters.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No chapters found for isbn=" + isbn + ". 분석이 안 된 책일 수 있습니다."
            );
        }

        // 3. 모든 AI 음악 후보 목록 조회
        List<AIMusic> allCandidates = aiMusicRepository.findAll();

        if (allCandidates.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No AI music candidates available."
            );
        }

        List<ChapterPlaylistResponse> chapterPlaylistResponses = new ArrayList<>();
        int totalMusicCount = 0;

        // 4. 각 챕터에 대해 "매번 새로" 챕터플레이리스트 구성 (DB 재사용 X)
        for (Chapter chapter : chapters) {
            Integer chapterNum = chapter.getChapterNum();
            log.info("Building chapter playlist (in-memory only). isbn={}, chapter={}", isbn, chapterNum);

            List<MusicResponse> musicResponses =
                    buildChapterPlaylistBySimilarity(chapter, allCandidates);

            totalMusicCount += musicResponses.size();

            chapterPlaylistResponses.add(
                    ChapterPlaylistResponse.builder()
                            .chapterNum(chapterNum)
                            .musics(musicResponses)
                            .build()
            );
        }

        // 5. 최종 BookPlaylistResponse 구성해서 반환
        return BookPlaylistResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .totalChapters(book.getTotalChapters())
                .totalMusic(totalMusicCount)
                .chapterPlaylist(chapterPlaylistResponses)
                .build();
    }

    /**
     * 하나의 챕터에 대한 챕터플레이리스트를 생성
     *
     * - ChapterAnalysis vs AIMusicAnalysis 유사도 기반으로 상위 N곡 선택
     * - DB에 Playlist/PlaylistAIMusic 저장 ❌
     * - 프론트용 MusicResponse(id만 포함) 목록만 반환
     */
    private List<MusicResponse> buildChapterPlaylistBySimilarity(
            Chapter chapter,
            List<AIMusic> candidates
    ) {
        ChapterAnalysis chapterAnalysis = chapter.getAnalysis();
        if (chapterAnalysis == null) {
            log.warn("Chapter {} has no analysis. isbn={}", chapter.getChapterNum(), chapter.getIsbn());
            return Collections.emptyList();
        }

        // 1. 각 음악에 대해 유사도 점수 계산
        List<ScoredMusic> scored = candidates.stream()
                .filter(m -> m.getAnalysis() != null)
                .map(m -> new ScoredMusic(m,
                        calculateSimilarity(chapterAnalysis, m.getAnalysis())))
                .filter(sm -> sm.score() > 0) // 점수 0 이하는 버려도 됨
                .sorted(Comparator.comparingInt(ScoredMusic::score).reversed())
                .collect(Collectors.toList());

        // 2. 상위 N개 선택
        List<ScoredMusic> selected = scored.stream()
                .limit(MUSIC_PER_CHAPTER)
                .collect(Collectors.toList());

        // 3. 선택된 곡들에 대해 MusicResponse 생성
        return selected.stream()
                .map(sm -> MusicResponse.builder()
                        .id(sm.music().getId())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 아주 간단한 유사도 계산 예시
     *
     * - main_mood가 같으면 +10
     * - 장르 교집합 1개당 +3
     * - 악기 교집합 1개당 +2
     * - 템포 교집합 1개당 +2
     * - 키워드 교집합 1개당 +1
     */
    private int calculateSimilarity(ChapterAnalysis chapter, AIMusicAnalysis music) {
        if (chapter == null || music == null) return 0;

        int score = 0;

        // main mood
        if (Objects.equals(chapter.getMainMood(), music.getMainMood())) {
            score += 10;
        }

        // 장르
        score += 3 * intersectionSize(
                chapter.getGenres(),
                music.getSelectedGenres()
        );

        // 악기
        score += 2 * intersectionSize(
                chapter.getInstruments(),
                music.getSelectedInstruments()
        );

        // 템포
        score += 2 * intersectionSize(
                chapter.getTempo(),
                music.getSelectedTempo()
        );

        // 키워드
        score += 1 * intersectionSize(
                chapter.getKeywords(),
                music.getSelectedKeywords()
        );

        return score;
    }

    /**
     * 두 리스트의 (소문자 기준) 교집합 원소 개수 계산
     */
    private int intersectionSize(List<String> a, List<String> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0;
        }

        Set<String> setA = a.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> setB = b.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        setA.retainAll(setB);
        return setA.size();
    }

    /**
     * 유사도 점수를 함께 들고 다니기 위한 간단한 record
     */
    private record ScoredMusic(AIMusic music, int score) {}
}
