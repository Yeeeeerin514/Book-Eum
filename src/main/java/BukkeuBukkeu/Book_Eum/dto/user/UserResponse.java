package BukkeuBukkeu.Book_Eum.dto.user;

import BukkeuBukkeu.Book_Eum.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 서버 -> 클라이언트
// 내 정보 조회 등에서 클라이언트에게 전달하는 객체

@Getter
@Builder
public class UserResponse {

    private String id;
    private String name;

    // 엔티티 → DTO로 변환
    public static UserResponse toDTO(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}