package me.nam.dreamdriversserver.domain.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "BusLive")
public class BusLive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_live_id")
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Buses bus;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20)
    private BusState state; // 운행 상태

    @Column(name = "lat", precision = 11, scale = 8)
    private BigDecimal lat; // 위도

    @Column(name = "lng", precision = 11, scale = 8)
    private BigDecimal lng; // 경도

    @Column(name = "current_stop_id")
    private Long currentStopId; // 현재 정류장 ID

    @Column(name = "next_stop_id")
    private Long nextStopId; // 다음 정류장 ID

    @Column(name = "eta_to_next_sec")
    private Integer etaToNextSec; // 다음 정류장까지 남은 초

    @Column(name = "remaining_dwell_sec")
    private Integer remainingDwellSec; // 정차 남은 초

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일
}