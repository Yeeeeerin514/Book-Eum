package BukkeuBukkeu.Book_Eum.service.user;

import BukkeuBukkeu.Book_Eum.auth.JwtProvider;
import BukkeuBukkeu.Book_Eum.domain.user.User;
import BukkeuBukkeu.Book_Eum.dto.user.AuthResponse;
import BukkeuBukkeu.Book_Eum.dto.user.UserJoinRequest;
import BukkeuBukkeu.Book_Eum.dto.user.UserLoginRequest;
import BukkeuBukkeu.Book_Eum.dto.user.UserResponse;
import BukkeuBukkeu.Book_Eum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    @Transactional
    public AuthResponse signup(UserJoinRequest request) {

        // 1) 아이디 중복 체크
        if (userRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2) 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3) DTO -> 엔티티 변환 (UserJoinRequest.toEntity(encodedPassword) 사용)
        User user = request.toEntity(encodedPassword);

        // 4) DB에 저장
        User saved = userRepository.save(user);

        // 5) 응답 DTO 변환
        UserResponse userResponse = UserResponse.toDTO(saved);

        // 6) 생성된 JWT 토큰
        String accessToken = jwtProvider.generateAccessToken(user.getId());

        return AuthResponse.builder()
                .user(userResponse)
                .accessToken(accessToken)
                .build();
    }

    // 로그인
    public AuthResponse login(UserLoginRequest request) {

        // 1) 아이디로 유저 조회
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) 응답 DTO 변환
        UserResponse userResponse = UserResponse.toDTO(user);

        // 4) 생성된 JWT 토큰
        String accessToken = jwtProvider.generateAccessToken(user.getId());

        return AuthResponse.builder()
                .user(userResponse)
                .accessToken(accessToken)
                .build();
    }
}
