package BukkeuBukkeu.Book_Eum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 회원가입/로그인 성공 시 서버가 access token을 발급하여 클라이언트에게 전달하는 객체

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private UserResponse user;  // 유저 정보
    private String accessToken; // 인증 토큰
}