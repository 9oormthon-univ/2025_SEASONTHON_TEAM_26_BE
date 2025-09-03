package me.nam.dreamdriversserver.domain.bus.repository;

import me.nam.dreamdriversserver.domain.bus.entity.Stops;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

/**
 * 정류장(Stops) 조회 리포지토리
 * - 위경도와 지역 ID 기준으로 가장 가까운 정류장을 찾는 네이티브 쿼리를 제공합니다.
 */
public interface StopsRepository extends JpaRepository<Stops, Long> {
    /**
     * 주어진 위경도(lat,lng)와 지역 ID에 대해 가장 가까운 정류장을 1건 반환합니다.
     * 거리 계산은 Haversine 공식을 사용합니다(반경 6371000m).
     */
    @Query(value = "SELECT s.* , (6371000 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(s.lat)))) AS distance " +
            "FROM stops s WHERE s.region_id = :regionId ORDER BY distance ASC LIMIT 1", nativeQuery = true)
    Optional<Stops> findNearestStop(@Param("lat") double lat, @Param("lng") double lng, @Param("regionId") Long regionId);

    /**
     * 지역 필터 없이 전체 정류장 중에서 가장 가까운 정류장 1건을 반환합니다.
     */
    @Query(value = "SELECT s.* , (6371000 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(s.lat)))) AS distance " +
            "FROM stops s ORDER BY distance ASC LIMIT 1", nativeQuery = true)
    Optional<Stops> findNearestStopAll(@Param("lat") double lat, @Param("lng") double lng);
}
