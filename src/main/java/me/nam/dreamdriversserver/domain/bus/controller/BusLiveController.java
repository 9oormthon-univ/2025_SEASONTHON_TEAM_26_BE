package me.nam.dreamdriversserver.domain.bus.controller;

import me.nam.dreamdriversserver.domain.bus.dto.BusLiveResponseDto;
import me.nam.dreamdriversserver.domain.bus.service.BusLiveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// Swagger(OpenAPI) 주석 추가
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;                    // ⬅ 추가
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping
@Tag(name = "courses", description = "코스 실시간 API")
public class BusLiveController {

    private static final Logger log = LoggerFactory.getLogger(BusLiveController.class);

    private final BusLiveService busLiveService;

    public BusLiveController(BusLiveService busLiveService) {
        this.busLiveService = busLiveService;
    }

    @Operation(
            summary = "코스별 실시간 현황",
            description = "단일 버스가 운행하는 코스의 실시간 현황을 정류장 리스트 기준으로 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusLiveResponseDto.class),
                            examples = @ExampleObject(name = "CourseLiveExample", value = "{\n  \"courseId\": \"2001\",\n  \"serverTime\": \"2025-09-04T09:25:30+09:00\",\n  \"status\": \"STOPPED\",\n  \"progress\": { \"currentOrder\": 1, \"nextOrder\": 2 },\n  \"etaToNextSec\": 480,\n  \"dwellSeconds\": 75,\n  \"stops\": [\n    {\n      \"order\": 1,\n      \"stopId\": \"STP_1001\",\n      \"name\": \"능곡초교\",\n      \"position\": { \"lat\": 37.66301, \"lng\": 126.83201 },\n      \"live\": { \"dwellRemainingSec\": 75, \"etaFromNowSec\": null }\n    },\n    {\n      \"order\": 2,\n      \"stopId\": \"STP_1002\",\n      \"name\": \"정발산\",\n      \"position\": { \"lat\": 37.65788, \"lng\": 126.85877 },\n      \"live\": { \"dwellRemainingSec\": null, \"etaFromNowSec\": 480 }\n    }\n  ],\n  \"sourceUpdatedAt\": \"2025-09-04T09:25:12+09:00\",\n  \"cacheAgeSec\": 18,\n  \"ttlSec\": 30\n}"))),
            @ApiResponse(responseCode = "404", description = "실시간 운행 정보 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"code\": \"NOT_FOUND\", \"message\": \"실시간 운행 정보 없음\" }")))
    })
    @GetMapping("/courses/{courseId}/live")
    public ResponseEntity<?> getCourseLive(
            @Parameter(description = "코스 ID (내부 숫자 PK 문자열)", required = true)
            @PathVariable("courseId") String courseId,
            @Parameter(description = "타임존(예: Asia/Seoul). 기본 Asia/Seoul")
            @RequestParam(value = "tz", required = false) String tz
    ) {
        try {
            BusLiveResponseDto dto = busLiveService.getCourseLive(courseId, tz);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("code", "NOT_FOUND");
            body.put("message", "실시간 운행 정보 없음");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
    }
    @PostMapping("/bus-live/ingest")
    public ResponseEntity<?> ingest(@RequestBody Map<String, Object> payload) {
        // payload 예시 키: busId, courseId, currentStopId, nextStopId, lat, lng, status, etaToNext, dwellSeconds
        log.info("[INGEST] {}", payload);
        // TODO: 여기서 In-Memory 캐시나 Redis로 upsert 하도록 확장 가능
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("receivedAt", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
