package me.nam.dreamdriversserver.domain.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nam.dreamdriversserver.domain.application.entity.Regions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "Stops")
public class Stops {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stop_id")
    private Long stopId; // 정류장 ID, PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Regions region; // 지역 (FK)

    @Column(name = "stop_name", nullable = false, length = 255)
    private String stopName; // 정류장 이름

    @Column(name = "lat", precision = 11, scale = 8)
    private BigDecimal lat; // 위도

    @Column(name = "lng", precision = 11, scale = 8)
    private BigDecimal lng; // 경도

    @Column(name = "create_at")
    private LocalDateTime createAt; // 생성일

    @OneToMany(mappedBy = "stop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseStops> courseStops = new ArrayList<>();
}