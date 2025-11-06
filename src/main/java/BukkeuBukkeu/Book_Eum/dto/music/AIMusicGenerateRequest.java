package BukkeuBukkeu.Book_Eum.dto.music;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AIMusicGenerateRequest {

    private String prompt; // 클라이언트가 /analyze 응답에서 가져온 프롬프트
    private Integer durationSeconds; // 원하는 음악 길이 (초 단위, 지정하지 않으면 기본 값)
}
