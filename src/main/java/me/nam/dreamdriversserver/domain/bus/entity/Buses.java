package me.nam.dreamdriversserver.domain.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nam.dreamdriversserver.domain.application.entity.Regions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Buses")
public class Buses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Long busId; // 버스 ID, PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Regions region; // 지역 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 코스 (FK)

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusLive> busLives = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일
}
