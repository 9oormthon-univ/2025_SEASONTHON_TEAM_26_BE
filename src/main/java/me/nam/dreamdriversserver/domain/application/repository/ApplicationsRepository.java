package me.nam.dreamdriversserver.domain.application.repository;

import me.nam.dreamdriversserver.domain.application.entity.Applications;
import me.nam.dreamdriversserver.domain.bus.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Applications 엔티티용 리포지토리
 * - 신청 수 카운트 등 조회성 쿼리를 제공합니다.
 */
public interface ApplicationsRepository extends JpaRepository<Applications, Long> {
    /**
     * 특정 지역(region_id)과 서비스일(service_date)에 해당하는 신청 건수를 카운트합니다.
     * JPQL: Applications a에서 a.region.regionId(Regions PK)와 a.serviceDate(LocalDate)를 조건으로 COUNT
     *
     * @param regionId    지역 PK(숫자)
     * @param serviceDate 서비스 날짜(LocalDate)
     * @return 신청 건수
     */
    @Query("SELECT COUNT(a) FROM Applications a WHERE a.region.regionId = :regionId AND a.serviceDate = :serviceDate")
    long countByRegionIdAndServiceDate(@Param("regionId") Long regionId, @Param("serviceDate") LocalDate serviceDate);

    /**
     * 특정 지역(region_id)과 서비스일(service_date)에 해당하며, stop이 null이 아닌 신청 건수를 카운트합니다.
     * JPQL: Applications a에서 a.region.regionId(Regions PK)와 a.serviceDate(LocalDate), a.stop IS NOT NULL 조건으로 COUNT
     *
     * @param regionId    지역 PK(숫자)
     * @param serviceDate 서비스 날짜(LocalDate)
     * @return 신청 건수
     */
    @Query("SELECT COUNT(a) FROM Applications a WHERE a.region.regionId = :regionId AND a.serviceDate = :serviceDate AND a.stop IS NOT NULL")
    long countWithStop(@Param("regionId") Long regionId, @Param("serviceDate") LocalDate serviceDate);

    /**
     * 지역/날짜/요일 기준 정류장별 신청자 수 집계
     * JPQL: Applications a에서 a.region.regionId(Regions PK), a.serviceDate(LocalDate), a.assignedDow(DayOfWeek), a.stop IS NOT NULL 조건으로 GROUP BY a.stop.stopId
     *
     * @param regionId    지역 PK(숫자)
     * @param serviceDate 서비스 날짜(LocalDate)
     * @param dow         요일(DayOfWeek)
     * @return 정류장별 신청자 수 목록
     */
    @Query("SELECT a.stop.stopId, COUNT(a) FROM Applications a WHERE a.region.regionId = :regionId AND a.serviceDate = :serviceDate AND a.assignedDow = :dow AND a.stop IS NOT NULL GROUP BY a.stop.stopId")
    List<Object[]> countByRegionDateDowGroupByStop(@Param("regionId") Long regionId, @Param("serviceDate") LocalDate serviceDate, @Param("dow") DayOfWeek dow);

    /**
     * 요일 기준(날짜 무시) 정류장별 신청자 수 집계
     * JPQL: Applications a에서 a.region.regionId(Regions PK), a.assignedDow(DayOfWeek), a.stop IS NOT NULL 조건으로 GROUP BY a.stop.stopId
     *
     * @param regionId 지역 PK(숫자)
     * @param dow      요일(DayOfWeek)
     * @return 정류장별 신청자 수 목록
     */
    @Query("SELECT a.stop.stopId, COUNT(a) FROM Applications a WHERE a.region.regionId = :regionId AND a.assignedDow = :dow AND a.stop IS NOT NULL GROUP BY a.stop.stopId")
    List<Object[]> countByRegionDowGroupByStop(@Param("regionId") Long regionId, @Param("dow") DayOfWeek dow);

    /**
     * 서비스일 기준 전체 신청 목록 (createdAt asc)
     *
     * @param regionId    지역 PK(숫자)
     * @param serviceDate 서비스 날짜(LocalDate)
     * @return 신청 목록
     */
    List<Applications> findByRegion_RegionIdAndServiceDateOrderByCreatedAtAsc(Long regionId, LocalDate serviceDate);

    /**
     * 요일 배정 여부 판단용 (assignedDow not null)
     *
     * @param regionId    지역 PK(숫자)
     * @param serviceDate 서비스 날짜(LocalDate)
     * @return 신청 건수
     */
    @Query("SELECT COUNT(a) FROM Applications a WHERE a.region.regionId = :regionId AND a.serviceDate = :serviceDate AND a.assignedDow IS NOT NULL")
    long countAssigned(@Param("regionId") Long regionId, @Param("serviceDate") LocalDate serviceDate);
}
