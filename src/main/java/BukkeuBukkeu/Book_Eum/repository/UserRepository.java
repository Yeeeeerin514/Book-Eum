package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> { // <엔티티 타입, PK 타입>

    // 로그인용 아이디로 (userId) 유저 조회
    Optional<User> findById(String id);

    // 로그인용 아이디 (userId) 중복 체크
    boolean existsById(String id);
}