package BukkeuBukkeu.Book_Eum.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * epub 파일을 로컬 디렉토리에 저장하고,
     * 저장된 파일의 "로컬 경로(String)" 를 반환
     */
    String storeEpub(MultipartFile file, String isbn);

    /**
     * AI 생성 음악을 클라우드에 업로드하고, 접근 가능한 URL을 반환
     * @param data 업로드할 AI 생성 음악 파일
     * @return 클라우드에 업로드된 음악 파일의 공개 URL
     */
    //String uploadMusicFile(byte[] data, String objectPath);
}
