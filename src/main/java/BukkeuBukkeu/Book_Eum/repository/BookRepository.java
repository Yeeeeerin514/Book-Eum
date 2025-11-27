package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface BookRepository extends JpaRepository<Book, String> { // <엔티티 타입, PK 타입>

    // 도서 등록 : JPA가 제공하는 save() 사용

    // isbn으로 도서 찾기
    Optional<Book> findByIsbn(String isbn);

    // 여러 isbn으로 한 번에 도서 조회 <- 있어야 하는 메소드인지 논의 필요 (도서 추천이라면 필요할 수도)
    List<Book> findByIsbnIn(Collection<String> isbns);

    // isbn으로 도서 존재 여부 확인
    boolean existsByIsbn(String isbn);

    // 제목으로 도서 찾기 (대소문자 무시)
    List<Book> findByTitleContainingIgnoreCase(String title); // 시스템 내 사용
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable); // 유저에게 보여주는 용도 (페이지 분할)

    // 도서 삭제
    void deleteByIsbn(String isbn);
}
