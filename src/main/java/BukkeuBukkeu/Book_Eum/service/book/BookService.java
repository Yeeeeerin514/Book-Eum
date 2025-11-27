package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.dto.book.BookSearchResponse;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Book getByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("해당 ISBN의 도서를 찾을 수 없습니다 : " + isbn));
    }

    // 제목으로 도서 찾기
    @Transactional(readOnly = true) // DB에 데이터를 쓰지 않으므로 dirty checking 비활성화
    public BookSearchResponse searchByTitle(String query, Pageable pageable) {
        Page<Book> pageResult = bookRepository.findByTitleContainingIgnoreCase(query, pageable);
        return BookSearchResponse.fromPage(pageResult);
    }

    // DB에서 해당 ISBN 도서 삭제
    @Transactional
    public void deleteByIsbn(String isbn) {
        // 이 메소드에서 플레이리스트 삭제까지 같이 호출을 고려 중
        bookRepository.deleteByIsbn(isbn);
    }
}
