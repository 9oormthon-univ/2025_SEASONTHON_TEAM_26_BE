package me.nam.dreamdriversserver.domain.bus.repository;

import me.nam.dreamdriversserver.domain.bus.entity.Course;
import me.nam.dreamdriversserver.domain.bus.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

/**
 * 코스(Course) 조회 리포지토리
 * - 지역/요일 기준 활성 코스를 조회합니다.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT c FROM Course c WHERE c.region.regionId = :regionId AND c.dow = :dow AND c.active = true")
    Optional<Course> findActiveCourseByRegionAndDay(@Param("regionId") Long regionId, @Param("dow") DayOfWeek dow);

    @Query("SELECT c FROM Course c WHERE c.region.regionId = :regionId AND c.dow = :dow AND c.active = true ORDER BY c.startTime ASC")
    List<Course> findAllActiveByRegionAndDay(@Param("regionId") Long regionId, @Param("dow") DayOfWeek dow);
}
