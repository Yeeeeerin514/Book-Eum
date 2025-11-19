package BukkeuBukkeu.Book_Eum.dto.user;

import BukkeuBukkeu.Book_Eum.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 클라이언트 -> 서버
// 클라이언트가 회원가입을 위해 정보를 입력하여 회원가입 버튼을 눌렀을 때
// 클라이언트의 회원가입 요청 데이터를 서버 내부로 전달하기 위한 객체

@Getter
@Setter
@NoArgsConstructor
public class UserJoinRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 최소 4자, 최대 20자입니다.")
    private String id;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[!@#$%^&*()_+\\-={}:\"'<>?,./]).+$",
            message = "비밀번호에는 최소 1개의 특수문자가 포함되어야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    // DTO -> 엔티티로 변환
    public User toEntity(String encodedPassword){
        return User.builder()
                .id(id)
                .password(encodedPassword)
                .name(name)
                .build();
    }
}