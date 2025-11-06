package BukkeuBukkeu.Book_Eum.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * ePub 파일을 클라우드에 업로드하고, 접근 가능한 URL을 반환
     * @param isbn 이 책의 isbn
     * @param epubFile 업로드할 ePub 파일
     * @return 클라우드에 업로드된 파일의 공개 URL
     */
    String uploadEpub(String isbn, MultipartFile epubFile);

    /**
     * AI 생성 음악을 클라우드에 업로드하고, 접근 가능한 URL을 반환
     * @param data 업로드할 AI 생성 음악 파일
     * @return 클라우드에 업로드된 음악 파일의 공개 URL
     */
    String uploadMusicFile(byte[] data, String objectPath);
}
