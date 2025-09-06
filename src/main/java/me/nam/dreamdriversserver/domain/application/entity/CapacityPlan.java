package me.nam.dreamdriversserver.domain.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nam.dreamdriversserver.domain.region.entity.Regions;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "CapacityPlan")
public class CapacityPlan {
    @Id
    @Column(name = "service_date")
    private LocalDate serviceDate; // 서비스 날짜, PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Regions region; // 지역 (FK)

    @Column(name = "capacity", nullable = false)
    private Integer capacity = 50; // 수용 인원, 기본값 50

}