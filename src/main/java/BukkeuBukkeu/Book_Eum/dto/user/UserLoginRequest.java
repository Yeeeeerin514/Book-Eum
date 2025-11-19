package BukkeuBukkeu.Book_Eum.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// 클라이언트 -> 서버
// 로그인할 때 전달 받는 객체

@Getter
public class UserLoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String id;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}