package me.nam.dreamdriversserver.domain.user.repository;

import me.nam.dreamdriversserver.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    // 사용자 관련 쿼리 메서드 작성 예정
}

