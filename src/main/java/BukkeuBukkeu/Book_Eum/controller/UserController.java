package BukkeuBukkeu.Book_Eum.controller;

import BukkeuBukkeu.Book_Eum.dto.user.AuthResponse;
import BukkeuBukkeu.Book_Eum.dto.user.UserJoinRequest;
import BukkeuBukkeu.Book_Eum.dto.user.UserLoginRequest;
import BukkeuBukkeu.Book_Eum.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users") // API URL 상위 공통
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     * POST /users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody UserJoinRequest request) {
        AuthResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 새로운 리소스 생성 => HTTP 201 CREATED
    }

    /**
     * 로그인 API
     * POST /users/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response); // HTTP 200 OK
    }
}
