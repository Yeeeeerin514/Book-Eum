package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface BookRepository extends JpaRepository<Book, String> { // <엔티티 타입, PK 타입>

    // 도서 등록 : JPA가 제공하는 save() 사용

    // isbn으로 도서 찾기
    Optional<Book> findByIsbn(String isbn);

    // 여러 isbn으로 한 번에 조회
    List<Book> findByIsbnIn(Collection<String> isbns);

    // isbn으로 도서 존재 여부 확인
    boolean existsByIsbn(String isbn);

    // 제목으로 도서 찾기 (대소문자 무시)
    List<Book> findByTitleContainingIgnoreCase(String title);
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 저자로 도서 찾기 (대소문자 무시)
    List<Book> findByAuthorContainingIgnoreCase(String author);
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // 장르로 도서 찾기
    List<Book> findByGenresContainingIgnoreCase(String genre);
    Page<Book> findByGenresContainingIgnoreCase(String genre, Pageable pageable);

    // 도서 삭제
    void deleteByIsbn(String isbn);
}
