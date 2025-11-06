package BukkeuBukkeu.Book_Eum.external;

public interface MusicAi {

    /**
     * 음악 생성 AI를 호출해 prompt에 맞는 음악을 생성하고
     * 오디오 데이터를 byte[]로 반환한다.
     */
    byte[] generateMusic(String musicPrompt, int durationSeconds);
}
