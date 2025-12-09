package BukkeuBukkeu.Book_Eum.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// 클라이언트 -> 서버
// 신규 도서 등록할 때 전달 받는 도서 객체

@Setter
@Getter
@NoArgsConstructor
public class BookRegisterRequest {

    @NotBlank // Controller에서 @Valid BookRegisterRequest request를 통해 자동으로 400 Bad Request 응답을 처리를 위함
    private String isbn;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    private String plot;

    @NotNull
    private MultipartFile epubFile; // epub 파일 자체

    public BookRegisterRequest(String isbn, String title, String author, String plot, MultipartFile epubFile) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.plot = plot;
        this.epubFile = epubFile;
    }
}
