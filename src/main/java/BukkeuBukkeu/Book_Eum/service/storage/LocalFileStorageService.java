package BukkeuBukkeu.Book_Eum.service.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;

// 로컬에 파일을 저장하는 FileStorageService 인터페이스의 구현 클래스

@Slf4j
@Component
public class LocalFileStorageService implements FileStorageService {

    private final Path localEpubPath;

//    @Value("${storage.music.local-dir:/Users/luna/CAU/2025-2/Capston Design 1/music}")
//    private String localMusicDir;

    public LocalFileStorageService(
            @Value("${app.storage.epub-dir:./data/epub}") String epubDir) {
        this.localEpubPath = Paths.get(epubDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(localEpubPath);
            log.info("EPUB file store directory: {}", localEpubPath);
        } catch (IOException e) {
            throw new RuntimeException("EPUB 업로드 디렉토리를 생성할 수 없습니다: " + localEpubPath, e);
        }
    }

    @Override
    public String storeEpub(MultipartFile file, String isbn) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 EPUB 파일이 비어 있습니다.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = ".epub";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // isbn.epub 이런 식으로 저장
        String storedFileName = isbn + extension;
        Path targetLocation = localEpubPath.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("EPUB 파일 저장에 실패했습니다: " + originalFilename, e);
        }

        // DB에는 로컬 경로를 그대로 저장
        return targetLocation.toString();
    }

//    @Override
//    public String uploadMusicFile(byte[] data, String objectPath) {
//        try {
//            File dir = new File(localMusicDir);
//            if (!dir.exists()) {
//                boolean created = dir.mkdirs();
//                log.info("Created local music dir: {} (success={})", localMusicDir, created);
//            }
//
//            File target = new File(dir, objectPath); // objectPath 예: "9788996-ch1.mp3"
//            File parent = target.getParentFile();
//            if (parent != null && !parent.exists()) {
//                parent.mkdirs();
//            }
//
//            try (FileOutputStream fos = new FileOutputStream(target)) {
//                fos.write(data);
//            }
//
//            log.info("Saved music file: {}", target.getAbsolutePath());
//
//            // 중간 발표용: 그냥 절대경로를 문자열로 반환해도 OK
//            // 나중에 정식 배포에서는 CDN/S3 URL로 교체
//            return target.getAbsolutePath();
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to save music file", e);
//        }
//    }
}
