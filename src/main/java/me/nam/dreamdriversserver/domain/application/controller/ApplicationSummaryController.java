package me.nam.dreamdriversserver.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nam.dreamdriversserver.domain.application.dto.ApplicationSummaryResponseDto;
import me.nam.dreamdriversserver.domain.application.service.ApplicationSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

/**
 * 신청 현황 요약 API 컨트롤러
 * - [GET] /applications/summary
 * - 요청: regionId(문자, 숫자 PK 문자열), date(YYYY-MM-DD)
 * - 응답: capacity/appliedCount/remaining/fillRatePercent 포함 DTO
 * - 에러: 데이터 없으면 404 { code: NOT_FOUND, message: 신청 현황 데이터 없음 }
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "버스 신청 현황 요약 API")
public class ApplicationSummaryController {

    private final ApplicationSummaryService applicationSummaryService;

    @Operation(summary = "버스 신청 현황 요약", description = "버스 신청 인원/정원/잔여/충족률 요약 제공")
    @GetMapping("/summary")
    public ResponseEntity<ApplicationSummaryResponseDto> getSummary(
            @Parameter(description = "지역 ID(숫자 PK 문자열)", required = true)
            @RequestParam String regionId,
            @Parameter(description = "기준 날짜(YYYY-MM-DD)", required = true)
            @RequestParam String date
    ) {
        // 예외는 서비스에서 던지고, 전역 핸들러가 처리
        ApplicationSummaryResponseDto result = applicationSummaryService.getSummary(regionId, date);
        return ResponseEntity.ok(result);
    }
}
