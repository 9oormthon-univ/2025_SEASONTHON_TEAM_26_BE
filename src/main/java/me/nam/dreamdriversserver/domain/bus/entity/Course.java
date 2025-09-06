package me.nam.dreamdriversserver.domain.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.nam.dreamdriversserver.domain.region.entity.Regions;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId; // 코스 ID, PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Regions region; // 지역 (FK)

    @Enumerated(EnumType.STRING)
    @Column(name = "dow", length = 10)
    private DayOfWeek dow; // 요일

    @Column(name = "start_time")
    private LocalTime startTime; // 시작 시간

    @Column(name = "dwell_min")
    private Integer dwellMin = 30; // 정차 시간(분), 기본값 30

    @Column(name = "travel_min")
    private Integer travelMin = 30; // 이동 시간(분), 기본값 30

    @Column(name = "active")
    private Boolean active; // 활성화 여부

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseStops> courseStops = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Buses> buses = new ArrayList<>();
}
