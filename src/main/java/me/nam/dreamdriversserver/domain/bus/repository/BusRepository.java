package me.nam.dreamdriversserver.domain.bus.repository;

import me.nam.dreamdriversserver.domain.bus.entity.Buses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Buses, Long> {
    // 버스 관련 쿼리 메서드 작성 예정
}

