package me.nam.dreamdriversserver.domain.bus.repository;

import me.nam.dreamdriversserver.domain.bus.entity.BusLive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusLiveRepository extends JpaRepository<BusLive, Long> {

    // BusLive 엔티티에 존재하는 컬럼명과 정확히 일치해야 함
    List<BusLive> findByCurrentStopId(Long currentStopId);
}