package me.nam.dreamdriversserver.domain.bus.repository;

import me.nam.dreamdriversserver.domain.bus.entity.CourseStops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * 코스-정류장 매핑 조회 리포지토리
 * - 특정 코스에 속한 정류장 목록을 경로 순서(stop_order)대로 반환합니다.
 */
public interface CourseStopsRepository extends JpaRepository<CourseStops, Long> {
    @Query("SELECT cs FROM CourseStops cs WHERE cs.course.courseId = :courseId ORDER BY cs.stopOrder ASC")
    List<CourseStops> findByCourseIdOrderByStopOrder(@Param("courseId") Long courseId);

    @Query("SELECT cs FROM CourseStops cs WHERE cs.stop.stopId = :stopId AND cs.course.dow = :dow AND cs.course.active = true ORDER BY cs.course.startTime ASC")
    List<CourseStops> findActiveByStopAndDow(@Param("stopId") Long stopId, @Param("dow") me.nam.dreamdriversserver.domain.bus.entity.DayOfWeek dow);
}
