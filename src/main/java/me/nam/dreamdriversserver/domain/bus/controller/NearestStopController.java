package me.nam.dreamdriversserver.domain.bus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.nam.dreamdriversserver.domain.bus.dto.NearestStopResponseDto;
import me.nam.dreamdriversserver.domain.bus.dto.StopDetailResponseDto;
import me.nam.dreamdriversserver.domain.bus.service.NearestStopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.common.exception.BadRequestException;
import me.nam.dreamdriversserver.common.exception.NotFoundException;

/**
 * 가장 가까운 정류장 API 컨트롤러
 * - GET /stops/nearest: 사용자의 위경도만으로 가장 가까운 정류장을 찾아 요약 정보 제공
 * - GET /stops/{stopId}: 정류장 상세(모달) 정보 제공
 * - 결과 없음 시 404 { code: NOT_FOUND, message: ... }
 */
@RestController
@RequestMapping("/stops")
@RequiredArgsConstructor
@Tag(name = "Stops", description = "가장 가까운 정류장/상세 API")
public class NearestStopController {
    private final NearestStopService nearestStopService;

    // 도메인 ID → 응답용 문자열 ID(so-XXX) 포맷터
    private static String formatStopId(Long id) {
        if (id == null) return null;
        return String.format("so-%03d", id);
    }

    // 요청 문자열 stopId 파싱(so-XXX 또는 숫자 문자열 지원)
    private static Long parseStopId(String stopId) {
        if (stopId == null) return null;
        String s = stopId.trim().toLowerCase();
        if (s.startsWith("so-")) s = s.substring(3);
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    /**
     * 사용자 위치 기준 최단 거리 정류장 조회(요약)
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @return { center:{lat,lng}, stopId, name }
     */
    @Operation(summary = "가장 가까운 정류장(요약)", description = "사용자 위치 기반 가장 가까운 정류장 요약 정보 조회")
    @GetMapping("/nearest")
    public ResponseEntity<?> getNearestStop(
            @Parameter(description = "사용자 위도", required = true)
            @RequestParam double lat,
            @Parameter(description = "사용자 경도", required = true)
            @RequestParam double lng
    ) {
        NearestStopResponseDto result = nearestStopService.getNearestStop(lat, lng);
        if (result == null) {
            throw new NotFoundException("주변 정류장 없음");
        }
        var body = new NearestStopSimpleResponse(
                new Center(result.getUserLat(), result.getUserLng()),
                formatStopId(result.getStopId()),
                result.getName()
        );
        return ResponseEntity.ok(body);
    }

    /**
     * 정류장 상세(모달)
     * @param stopId 경로 변수(so-XXX 또는 숫자)
     * @return { stopId, regionId, name, nextArrivalTime, dwellSeconds }
     */
    @Operation(summary = "정류장 상세", description = "정류장 아이디로 상세 정보 조회")
    @GetMapping("/{stopId}")
    public ResponseEntity<?> getStopDetail(
            @Parameter(description = "정류장 아이디(so-XXX 또는 숫자)", required = true)
            @PathVariable String stopId
    ) {
        Long id = parseStopId(stopId);
        if (id == null) {
            throw new BadRequestException("유효하지 않은 stopId");
        }
        StopDetailResponseDto dto = nearestStopService.getStopDetail(id);
        if (dto == null) {
            throw new NotFoundException("정류장 없음");
        }
        var body = new StopDetailSimpleResponse(
                formatStopId(dto.getStopId()),
                String.valueOf(dto.getRegionId()),
                dto.getName(),
                dto.getNextArrivalTime(),
                dto.getDwellSeconds()
        );
        return ResponseEntity.ok(body);
    }

    /** summary 응답 스펙 */
    static class NearestStopSimpleResponse {
        public Center center; // 내 위치
        public String stopId;
        public String name;
        public NearestStopSimpleResponse(Center center, String stopId, String name) {
            this.center = center;
            this.stopId = stopId;
            this.name = name;
        }
    }

    /** stop detail 응답 스펙 */
    static class StopDetailSimpleResponse {
        public String stopId;
        public String regionId;
        public String name;
        public String nextArrivalTime;
        public int dwellSeconds;
        public StopDetailSimpleResponse(String stopId, String regionId, String name, String nextArrivalTime, int dwellSeconds) {
            this.stopId = stopId;
            this.regionId = regionId;
            this.name = name;
            this.nextArrivalTime = nextArrivalTime;
            this.dwellSeconds = dwellSeconds;
        }
    }

    /** 좌표 래퍼 */
    static class Center {
        public double lat;
        public double lng;
        public Center(double lat, double lng) { this.lat = lat; this.lng = lng; }
    }
}
