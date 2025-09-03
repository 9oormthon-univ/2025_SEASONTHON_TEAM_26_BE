package me.nam.dreamdriversserver.domain.application.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.application.dto.ApplicationSummaryResponseDto;
import me.nam.dreamdriversserver.domain.application.repository.ApplicationsRepository;
import me.nam.dreamdriversserver.domain.region.entity.Regions;
import me.nam.dreamdriversserver.domain.region.repository.RegionsRepository;
import org.springframework.stereotype.Service;

/**
 * 신청 현황 요약 비즈니스 로직
 * - 입력: regionId(String, 숫자 PK 문자열), date(String, ISO-8601: YYYY-MM-DD)
 * - 처리:
 *   1) regionId를 Long으로 파싱(숫자 아님 → null 반환 → 컨트롤러 404 매핑)
 *   2) date를 LocalDate로 파싱(형식 불일치 시 런타임 예외 → 스프링 기본 400 처리)
 *   3) Applications에서 (region, serviceDate) 조건으로 신청 건수 카운트
 *   4) capacity/appliedCount/remaining/fillRatePercent 계산 후 DTO 반환
 * - 출력: ApplicationSummaryResponseDto 또는 null(컨트롤러에서 404 변환)
 */
@Service
@RequiredArgsConstructor
public class ApplicationSummaryService {
    private static final int FIXED_CAPACITY = 50; // 정원 50 고정

    private final ApplicationsRepository applicationsRepository; // 신청 카운트 조회
    private final RegionsRepository regionsRepository; // 지역 존재 검증 및 이름 조회

    /**
     * 버스 신청 현황 요약을 계산한다.
     * - capacity: 50 고정
     * - appliedCount: 해당 region/date 신청 건수
     * - remaining: max(0, capacity - appliedCount) (좌석 잔여)
     * - fillRatePercent: 신청 1명당 2% 증가(appliedCount * 2.0)
     */
    public ApplicationSummaryResponseDto getSummary(String regionId, String date) {
        Long regionPk;
        try {
            regionPk = Long.parseLong(regionId);
        } catch (NumberFormatException e) {
            return null; // 잘못된 regionId 포맷 → 404 처리
        }

        Regions region = regionsRepository.findById(regionPk).orElse(null);
        if (region == null) {
            return null;
        }

        LocalDate serviceDate = LocalDate.parse(date);

        long appliedCount = applicationsRepository.countByRegionIdAndServiceDate(regionPk, serviceDate);

        int capacity = FIXED_CAPACITY;
        int remaining = Math.max(0, capacity - (int) appliedCount);
        double fillRatePercent = appliedCount * 2.0; // 2% 규칙

        return new ApplicationSummaryResponseDto(
                regionId,
                region.getName(),
                date,
                capacity,
                appliedCount,
                remaining,
                fillRatePercent
        );
    }
}
