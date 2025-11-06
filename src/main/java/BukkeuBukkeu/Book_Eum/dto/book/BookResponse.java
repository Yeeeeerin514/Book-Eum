package BukkeuBukkeu.Book_Eum.dto.book;

import BukkeuBukkeu.Book_Eum.domain.Book;
import lombok.Builder;
import lombok.Getter;

// 단일 도서를 조회할 때 서버가 클라이언트에게 반환하는 객체

@Getter
@Builder
public class BookResponse {

    private String isbn;
    private String title;
    private String author;
    private String genres;
    private String epubFileUrl;
    private boolean isAnalyzed;

    // Entity -> DTO 변환
    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .isbn(String.valueOf(book.getIsbn()))
                .title(book.getTitle())
                .author(book.getAuthor())
                .genres(book.getGenres())
                .epubFileUrl(book.getEpubFileUrl())
                .isAnalyzed(book.getIsAnalyzed())
                .build();
    }
}
