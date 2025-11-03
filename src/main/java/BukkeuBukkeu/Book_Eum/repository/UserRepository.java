package BukkeuBukkeu.Book_Eum.repository;

import BukkeuBukkeu.Book_Eum.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> { // <엔티티 타입, PK 타입>

    // userId로 유저 찾기
    Optional<User> findByUserId(String userId);

    // 아이디 중복 체크
    boolean existsByUserId(String userId);
}