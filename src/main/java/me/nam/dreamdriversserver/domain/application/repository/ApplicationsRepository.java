package me.nam.dreamdriversserver.domain.application.repository;

import me.nam.dreamdriversserver.domain.application.entity.Applications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

/**
 * Applications 엔티티용 리포지토리
 * - 신청 수 카운트 등 조회성 쿼리를 제공합니다.
 */
public interface ApplicationsRepository extends JpaRepository<Applications, Long> {
    /**
     * 특정 지역(region_id)과 서비스일(service_date)에 해당하는 신청 건수를 카운트합니다.
     * JPQL: Applications a에서 a.region.regionId(Regions PK)와 a.serviceDate(LocalDate)를 조건으로 COUNT
     * @param regionId 지역 PK(숫자)
     * @param serviceDate 서비스 날짜(LocalDate)
     * @return 신청 건수
     */
    @Query("SELECT COUNT(a) FROM Applications a WHERE a.region.regionId = :regionId AND a.serviceDate = :serviceDate")
    long countByRegionIdAndServiceDate(@Param("regionId") Long regionId, @Param("serviceDate") LocalDate serviceDate);
}

