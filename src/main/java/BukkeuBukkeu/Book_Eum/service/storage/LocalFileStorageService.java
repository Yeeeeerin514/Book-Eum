package BukkeuBukkeu.Book_Eum.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// 중간 발표용 : 로컬에 저장하는 구현 클래스

@Slf4j
@Component
public class LocalFileStorageService implements FileStorageService {

    @Value("${storage.epub.local-dir:/Users/luna/CAU/2025-2/Capston Design 1/epub}")
    private String localEpubDir;

    @Value("${storage.music.local-dir:/Users/luna/CAU/2025-2/Capston Design 1/music}")
    private String localMusicDir;

    @Override
    public String uploadEpub(String isbn, MultipartFile epubFile) {
        try {
            // 1. ePub 저장 디렉토리 생성
            File dir = new File(localEpubDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 파일명: ISBN.epub <- 바꿔야 됨!!!
            String fileName = isbn + ".epub";
            File target = new File(dir, fileName);

            // 3. 파일 저장
            epubFile.transferTo(target);

            log.info("[FileStorage] Saved ePub file for ISBN {} at: {}", isbn, target.getAbsolutePath());

            // 4. URL로 사용할 문자열 반환
            // 실제 배포에서는 클라우드 URL, 지금은 절대경로
            return target.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload ePub file", e);
        }
    }

    @Override
    public String uploadMusicFile(byte[] data, String objectPath) {
        try {
            File dir = new File(localMusicDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("Created local music dir: {} (success={})", localMusicDir, created);
            }

            File target = new File(dir, objectPath); // objectPath 예: "9788996-ch1.mp3"
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(data);
            }

            log.info("Saved music file: {}", target.getAbsolutePath());

            // 중간 발표용: 그냥 절대경로를 문자열로 반환해도 OK
            // 나중에 정식 배포에서는 CDN/S3 URL로 교체
            return target.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException("Failed to save music file", e);
        }
    }
}
