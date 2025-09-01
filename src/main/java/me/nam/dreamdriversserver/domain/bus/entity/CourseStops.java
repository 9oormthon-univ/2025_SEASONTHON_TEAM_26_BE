package me.nam.dreamdriversserver.domain.bus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "CourseStops",
       uniqueConstraints = {
         @UniqueConstraint(name = "uk_course_stop_order", columnNames = {"course_id", "stop_order"})
       })
public class CourseStops {

    @Embeddable
    @Getter
    @Setter
    public static class CourseStopsId implements Serializable {
        @Column(name = "stop_id")
        private Long stopId;

        @Column(name = "course_id")
        private Long courseId;

        public CourseStopsId() {}
        public CourseStopsId(Long stopId, Long courseId) {
            this.stopId = stopId;
            this.courseId = courseId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CourseStopsId that = (CourseStopsId) o;
            return java.util.Objects.equals(stopId, that.stopId) &&
                   java.util.Objects.equals(courseId, that.courseId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(stopId, courseId);
        }
    }

    @EmbeddedId
    private CourseStopsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stopId")
    @JoinColumn(name = "stop_id", nullable = false)
    private Stops stop; // 정류장 (FK, PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 코스 (FK, PK)

    @Column(name = "stop_order", nullable = true)
    private Integer stopOrder; // 경로 내 순서

    @Column(name = "planned_arrival")
    private LocalTime plannedArrival; // 예정 도착 시간
}