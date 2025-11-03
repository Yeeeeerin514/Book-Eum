package BukkeuBukkeu.Book_Eum.dto;

import BukkeuBukkeu.Book_Eum.domain.User;
import lombok.Builder;
import lombok.Getter;

// 클라이언트 요청에 서버가 응답하여 클라이언트에게 보내는 객체

@Getter
@Builder
public class UserResponse {

    private String userId;
    private String name;

    // 엔티티 → DTO로 변환
    public static UserResponse toDTO(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .build();
    }
}