package me.nam.dreamdriversserver.domain.bus.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
     * @return {
     *   center:{lat,lng}, // 정류장 좌표
     *   stopId, stopname, regionId, regionName,
     *   distanceMeters, date(YYYY-MM-DD), etaToNextSec, dwellSeconds
     * }
     */
    @Operation(summary = "가장 가까운 정류장(요약)", description = "사용자 위치 기반 가장 가까운 정류장 요약 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NearestStopNewResponse.class),
                            examples = @ExampleObject(name = "NearestStopExample", value = "{\n  \"center\": { \"lat\": 37.4836, \"lng\": 127.0326 },\n  \"stopId\": \"so-002\",\n  \"stopname\": \"서초동도서관\",\n  \"regionId\": \"1001\",\n  \"regionName\": \"서울특별시 서초구\",\n  \"distanceMeters\": 184,\n  \"date\": \"2025-09-05\",\n  \"etaToNextSec\": 320,\n  \"dwellSeconds\": 120\n}"))),
            @ApiResponse(responseCode = "404", description = "주변 정류장 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"code\": \"NOT_FOUND\", \"message\": \"주변 정류장 없음\" }")))
    })
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
        String regionId = result.getRegionId() != null ? String.valueOf(result.getRegionId()) : null;
        String regionName = result.getRegionName();
        String date = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString(); // YYYY-MM-DD
        var body = new NearestStopNewResponse(
                new Center(result.getStopLat(), result.getStopLng()), // 정류장 좌표
                formatStopId(result.getStopId()),
                result.getName(),
                regionId,
                regionName,
                result.getDistanceMeters(),
                date,
                result.getEtaToNextSec(),
                result.getDwellSeconds()
        );
        return ResponseEntity.ok(body);
    }

    /**
     * 정류장 상세(모달)
     * @param stopId 경로 변수(so-XXX 또는 숫자)
     * @return { stopId, regionId, name, nextArrivalTime, dwellSeconds }
     */
    @Operation(summary = "정류장 상세", description = "정류장 아이디로 상세 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StopDetailNewResponse.class),
                            examples = @ExampleObject(name = "StopDetailExample", value = "{\n  \"center\": { \"lat\": 37.4836, \"lng\": 127.0326 },\n  \"stopId\": \"so-002\",\n  \"stopname\": \"서초동도서관\",\n  \"regionId\": \"1001\",\n  \"regionName\": \"서울특별시 서초구\",\n  \"distanceMeters\": 0,\n  \"date\": \"2025-09-05\",\n  \"etaToNextSec\": null,\n  \"dwellSeconds\": 0\n}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"code\": \"BAD_REQUEST\", \"message\": \"유효하지 않은 stopId\" }"))),
            @ApiResponse(responseCode = "404", description = "정류장 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"code\": \"NOT_FOUND\", \"message\": \"정류장 없음\" }")))
    })
    @GetMapping("/{stopId}")
    public ResponseEntity<?> getStopDetail(
            @Parameter(description = "정류장 아이디(so-XXX 또는 숫자)", required = true)
            @PathVariable String stopId,
            @Parameter(description = "사용자 위도(선택)") @RequestParam(required = false) Double lat,
            @Parameter(description = "사용자 경도(선택)") @RequestParam(required = false) Double lng
    ) {
        Long id = parseStopId(stopId);
        if (id == null) {
            throw new BadRequestException("유효하지 않은 stopId");
        }
        // 거리 계산을 위해 사용자 좌표 전달(없으면 0m 처리)
        StopDetailResponseDto dto = nearestStopService.getStopDetail(id, lat, lng);
        if (dto == null) {
            throw new NotFoundException("정류장 없음");
        }
        String regionId = dto.getRegionId() != null ? String.valueOf(dto.getRegionId()) : null;
        String date = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).toString();
        var body = new StopDetailNewResponse(
                new Center(dto.getStopLat(), dto.getStopLng()),
                formatStopId(dto.getStopId()),
                dto.getName(),
                regionId,
                dto.getRegionName(),
                dto.getDistanceMeters(),
                date,
                dto.getEtaToNextSec(),
                dto.getDwellSeconds()
        );
        return ResponseEntity.ok(body);
    }

    /** summary 응답 스펙(신규) */
    static class NearestStopNewResponse {
        @Schema(description = "정류장 좌표")
        public Center center; // 정류장 좌표
        @Schema(description = "정류장 식별자(so-포맷)", example = "so-002")
        public String stopId;
        @Schema(description = "정류장 이름", example = "서초동도서관")
        public String stopname;
        @Schema(description = "지역 ID(문자열)", example = "1001")
        public String regionId;
        @Schema(description = "지역 이름", example = "서울특별시 서초구")
        public String regionName;
        @Schema(description = "사용자 위치로부터 거리(미터)", example = "184")
        public int distanceMeters;
        @Schema(description = "조회일(yyyy-MM-dd)", example = "2025-09-05")
        public String date; // YYYY-MM-DD
        @Schema(description = "다음 정류장까지 ETA 초(null 가능)", example = "320")
        public Integer etaToNextSec; // null 가능
        @Schema(description = "현재 정류장 체류 예상/잔여 초", example = "120")
        public int dwellSeconds;
        public NearestStopNewResponse(Center center, String stopId, String stopname, String regionId, String regionName,
                                      int distanceMeters, String date, Integer etaToNextSec, int dwellSeconds) {
            this.center = center;
            this.stopId = stopId;
            this.stopname = stopname;
            this.regionId = regionId;
            this.regionName = regionName;
            this.distanceMeters = distanceMeters;
            this.date = date;
            this.etaToNextSec = etaToNextSec;
            this.dwellSeconds = dwellSeconds;
        }
    }

    /** stop detail 응답 스펙(신규) */
    static class StopDetailNewResponse {
        @Schema(description = "정류장 좌표")
        public Center center; // 정류장 좌표
        @Schema(description = "정류장 식별자(so-포맷)", example = "so-002")
        public String stopId; // so-XXX
        @Schema(description = "정류장 이름", example = "서초동도서관")
        public String stopname; // 정류장 이름
        @Schema(description = "지역 ID(문자열)", example = "1001")
        public String regionId; // 문자열
        @Schema(description = "지역 이름", example = "서울특별시 서초구")
        public String regionName;
        @Schema(description = "사용자 위치로부터 거리(미터)", example = "0")
        public int distanceMeters;
        @Schema(description = "조회일(yyyy-MM-dd)", example = "2025-09-05")
        public String date; // YYYY-MM-DD
        @Schema(description = "다음 정류장까지 ETA 초(null 가능)", example = "300")
        public Integer etaToNextSec; // null 가능
        @Schema(description = "현재 정류장 체류 예상/잔여 초", example = "0")
        public int dwellSeconds;
        public StopDetailNewResponse(Center center, String stopId, String stopname, String regionId, String regionName,
                                     int distanceMeters, String date, Integer etaToNextSec, int dwellSeconds) {
            this.center = center;
            this.stopId = stopId;
            this.stopname = stopname;
            this.regionId = regionId;
            this.regionName = regionName;
            this.distanceMeters = distanceMeters;
            this.date = date;
            this.etaToNextSec = etaToNextSec;
            this.dwellSeconds = dwellSeconds;
        }
    }

    /** 좌표 래퍼 */
    static class Center {
        @Schema(description = "위도", example = "37.4836")
        public double lat;
        @Schema(description = "경도", example = "127.0326")
        public double lng;
        public Center(double lat, double lng) { this.lat = lat; this.lng = lng; }
    }
}
