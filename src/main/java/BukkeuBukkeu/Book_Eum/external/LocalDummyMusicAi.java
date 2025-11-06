package BukkeuBukkeu.Book_Eum.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

// 중간 발표용 : 로컬 음악 파일 읽어오는 구현 클래스

@Slf4j
@Component
public class LocalDummyMusicAi implements MusicAi {

    // 미리 준비해둔 mp3 파일 경로 (예: src/main/resources/demo/demo-music.mp3 복사본)
    @Value("${musicai.dummy.file-path:/Users/luna/CAU/2025-2/Capston Design 1/music/demo-music.mp3}")
    private String dummyFilePath;

    @Override
    public byte[] generateMusic(String musicPrompt, int durationSeconds) {
        log.info("MusicAi.generate called. prompt='{}', duration={}sec", musicPrompt, durationSeconds);

        try {
            File f = new File(dummyFilePath);
            return Files.readAllBytes(f.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dummy music file: " + dummyFilePath, e);
        }
    }
}
