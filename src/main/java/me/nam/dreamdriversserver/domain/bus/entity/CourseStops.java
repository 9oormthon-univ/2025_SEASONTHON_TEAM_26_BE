package me.nam.dreamdriversserver.domain.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "CourseStops")
@IdClass(CourseStopsId.class)
public class CourseStops {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stops stop; // 정류장 (FK, PK)

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 코스 (FK, PK)

    @Column(name = "stop_order")
    private Integer stopOrder; // 경로 내 순서

    @Column(name = "planned_arrival")
    private LocalTime plannedArrival; // 예정 도착 시간

}