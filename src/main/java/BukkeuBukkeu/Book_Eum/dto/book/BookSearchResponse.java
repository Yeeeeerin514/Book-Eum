package BukkeuBukkeu.Book_Eum.dto.book;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

// 도서 검색할 때 서버가 클라이언트에게 반환하는 객체 ex) 제목 검색, isbn 검색 등

@Getter
@Builder
public class BookSearchResponse {

    private List<BookResponse> content;
    private int page; // 몇 번째 페이지를 보여줄건지
    private int size; // 한 페이지에 몇 개의 결과를 보여줄건지
    private long totalElements; // 총 검색 결과 개수
    private int totalPages; // 총 페이지 개수

    // Page<Entity> → DTO 변환
    public static BookSearchResponse fromPage(Page<?> pageResult) {

        List<BookResponse> content = pageResult.getContent().stream()
                .map(entity -> BookResponse.fromEntity((Book) entity))
                .collect(Collectors.toList());

        return BookSearchResponse.builder()
                .content(content)
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }
}
