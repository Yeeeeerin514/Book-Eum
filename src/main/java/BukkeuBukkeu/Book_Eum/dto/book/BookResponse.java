package BukkeuBukkeu.Book_Eum.dto.book;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import lombok.Builder;
import lombok.Getter;

// 책 한 권에 대한 데이터를 담는 dto (도서 열람 x)

@Getter
@Builder
public class BookResponse {

    private String isbn;
    private String title;
    private String author;
    private String plot;
    private String epubFilePath;

    // Entity -> DTO 변환
    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .plot(book.getPlot())
                .epubFilePath(book.getEpubFilePath())
                .build();
    }
}
