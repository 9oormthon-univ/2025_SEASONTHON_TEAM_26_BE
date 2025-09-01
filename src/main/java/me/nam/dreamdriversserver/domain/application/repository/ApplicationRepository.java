package me.nam.dreamdriversserver.domain.application.repository;

import me.nam.dreamdriversserver.domain.application.entity.Applications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Applications, Long> {
    // 신청 관련 쿼리 메서드 작성 예정
}

