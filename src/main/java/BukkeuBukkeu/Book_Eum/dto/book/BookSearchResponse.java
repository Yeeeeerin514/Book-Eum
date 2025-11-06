package BukkeuBukkeu.Book_Eum.dto.book;

import BukkeuBukkeu.Book_Eum.domain.Book;
import lombok.Builder;
import lombok.Getter;

// 도서 검색할 때 서버가 클라이언트에게 도서를 반환

@Getter
@Builder
public class BookSearchResponse {

    private String isbn;
    private String title;
    private String author;
    private String genres;

    // Entity -> DTO 변환
    public static BookSearchResponse fromEntity(Book book) {
        return BookSearchResponse.builder()
                .isbn(String.valueOf(book.getIsbn()))
                .title(book.getTitle())
                .author(book.getAuthor())
                .genres(book.getGenres())
                .build();
    }
}
