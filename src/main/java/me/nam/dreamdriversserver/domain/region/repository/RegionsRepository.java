package me.nam.dreamdriversserver.domain.region.repository;

import me.nam.dreamdriversserver.domain.region.entity.Regions;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RegionsRepository extends JpaRepository<Regions, Long> {
    // 지역명 부분 일치 검색 (대소문자 무시), 접두 일치 결과를 먼저 노출한 뒤 이름 오름차순 정렬.
    // Pageable 파라미터로 limit를 DB 레벨에서 적용하여 불필요한 데이터 로드를 줄임.
    @Query("""
           SELECT r
           FROM Regions r
           WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           ORDER BY
             CASE WHEN LOWER(r.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END,
             r.name ASC
           """)
    List<Regions> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 이름 목록(IN)으로 일괄 조회, 이름 오름차순
    @Query("SELECT r FROM Regions r WHERE r.name IN :names ORDER BY r.name ASC")
    List<Regions> findByNames(@Param("names") List<String> names);

    // 단일 이름으로 조회 (정확 일치)
    Regions findFirstByName(String name);
}
