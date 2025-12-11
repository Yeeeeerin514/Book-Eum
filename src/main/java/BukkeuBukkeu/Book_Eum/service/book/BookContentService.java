package BukkeuBukkeu.Book_Eum.service.book;

import BukkeuBukkeu.Book_Eum.domain.book.Book;
import BukkeuBukkeu.Book_Eum.repository.BookRepository;
import BukkeuBukkeu.Book_Eum.service.storage.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookContentService {

    private final BookRepository bookRepository;
    private final LocalFileStorageService fileStorageService;

    /**
     * ISBN으로 Book 조회 후, 해당 epub 파일을 Resource로 감싸서 반환
     */
    public BookFile getBookFile(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + isbn));

        String storedPath = book.getEpubFilePath();

        Resource resource = fileStorageService.loadEpub(storedPath);

        long size;
        try {
            size = Files.size(Paths.get(storedPath));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new BookFile(resource,
                Paths.get(storedPath).getFileName().toString(),
                size);
    }

    public record BookFile(Resource resource, String fileName, long contentLength) {}
}
