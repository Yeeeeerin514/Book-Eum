package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.Book;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// 도서 검색, 삭제 등 기본 기능

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    // isbn으로 도서 찾기
    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 도서를 찾을 수 없습니다 : " + isbn));
    }

    // 제목으로 도서 찾기
    @Transactional(readOnly = true)
    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    // 장르로 도서 찾기 (하나의 장르)
    @Transactional(readOnly = true)
    public List<Book> searchBooksByGenre(String genre) {
        return bookRepository.findByGenresContainingIgnoreCase(genre);
    }

    // 장르로 도서 찾기 (여러 장르 AND 조건)
    @Transactional(readOnly = true)
    public List<Book> searchBooksByAllGenres(List<String> genres) {
        return genres.stream()
                .map(bookRepository::findByGenresContainingIgnoreCase)
                .reduce((list1, list2) ->
                        list1.stream()
                                .filter(list2::contains)
                                .toList()
                )
                .orElse(List.of());
    }

    // DB에서 해당 ISBN 도서 삭제
    @Transactional
    public void deleteByIsbn(String isbn) {
        // 이 메소드에서 플레이리스트 삭제까지 같이 호출을 고려 중
        bookRepository.deleteByIsbn(isbn);
    }
}
