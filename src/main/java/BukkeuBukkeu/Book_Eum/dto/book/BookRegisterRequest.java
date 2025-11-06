package BukkeuBukkeu.Book_Eum.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// 신규 도서 등록할 때 도서 정보를 클라이언트에서 서버로 전달할 객체

@Getter
@NoArgsConstructor
public class BookRegisterRequest {

    private String isbn;
    private String title;
    private String author;
    private MultipartFile epubFile; // epub 파일 자체

    public BookRegisterRequest(String isbn, String title, String author, MultipartFile epubFile) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.epubFile = epubFile;
    }
}
