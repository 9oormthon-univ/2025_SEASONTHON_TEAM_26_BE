package me.nam.dreamdriversserver.domain.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nam.dreamdriversserver.domain.application.entity.Applications;
import me.nam.dreamdriversserver.domain.bus.entity.Buses;
import me.nam.dreamdriversserver.domain.bus.entity.Stops;
import me.nam.dreamdriversserver.domain.bus.entity.Course;
import me.nam.dreamdriversserver.domain.application.entity.CapacityPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Regions")
public class Regions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId; // 지역 ID, PK

    @Column(nullable = false, length = 100)
    private String name; // 지역 이름

    @Column(name = "center_lat", precision = 11, scale = 8)
    private BigDecimal centerLat; // 중심 위도

    @Column(name = "center_lng", precision = 11, scale = 8)
    private BigDecimal centerLng; // 중심 경도

    @Column(name = "create_at")
    private LocalDateTime createAt; // 생성일

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Applications> applications = new ArrayList<>();

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Buses> buses = new ArrayList<>();

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stops> stops = new ArrayList<>();

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CapacityPlan> capacityPlans = new ArrayList<>();
}