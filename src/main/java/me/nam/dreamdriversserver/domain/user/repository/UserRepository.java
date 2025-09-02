package me.nam.dreamdriversserver.domain.user.repository;

import me.nam.dreamdriversserver.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
    Optional<Users> findByLoginId(String loginId);
}

