package BukkeuBukkeu.Book_Eum.dto.music;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 음악 (정보o, wav파일x)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicResponse {

    private Long id;
}
