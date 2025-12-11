package BukkeuBukkeu.Book_Eum.service.music;

import BukkeuBukkeu.Book_Eum.domain.music.AIMusic;
import BukkeuBukkeu.Book_Eum.repository.AIMusicRepository;
import BukkeuBukkeu.Book_Eum.service.storage.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicFileService {

    private final AIMusicRepository aiMusicRepository;
    private final LocalFileStorageService localFileStorageService;

    public MusicFile getMusicFile(Long musicId) {

        AIMusic music = aiMusicRepository.findById(musicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Music not found: " + musicId));

        String storedPath = music.getAudioFilePath(); // 예: /Users/luna/data/music/123.wav

        if (storedPath == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Music file path missing for id=" + musicId);
        }

        Resource resource = localFileStorageService.loadAsResource(storedPath);

        try {
            Path path = Paths.get(storedPath);
            long size = Files.size(path);
            String fileName = path.getFileName().toString();
            return new MusicFile(resource, fileName, size);

        } catch (Exception e) {
            log.error("Failed to read music file: {}", storedPath, e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Music load error");
        }
    }

    public record MusicFile(Resource resource, String fileName, long fileSize) {}
}