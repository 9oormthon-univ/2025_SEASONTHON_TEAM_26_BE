package me.nam.dreamdriversserver.domain.region.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import me.nam.dreamdriversserver.domain.region.service.RegionsService;
import me.nam.dreamdriversserver.domain.region.dto.RegionServiceResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import me.nam.dreamdriversserver.domain.region.dto.RegionHierarchySearchResponseDto;

/**
 * 지역 API 컨트롤러
 * - GET /regions/search : 지역 계층 검색 (광역/기초)
 * - GET /regions/{regionId}/service : 특정 지역/날짜의 운행 정보
 */
@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
@Tag(name = "Regions", description = "지역 검색 및 운행 정보 API")
public class RegionsController {
    private final RegionsService regionsService;

    @Operation(
            summary = "지역 검색 (계층형)",
            description = "전국 지역을 광역(시·도)과 하위 기초(시·군·구) 계층 구조로 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegionHierarchySearchResponseDto.class),
                            examples = @ExampleObject(name = "RegionHierarchyExample", value = "{\n  \"regions\": [\n    {\n      \"regionId\": \"RGN_2B6C76E3\",\n      \"name\": \"서울특별시\",\n      \"children\": [\n        { \"regionId\": \"RGN_2B6C76E3_7F3A1C2D\", \"name\": \"종로구\" },\n        { \"regionId\": \"RGN_2B6C76E3_9A4B3D1E\", \"name\": \"중구\" }\n      ]\n    }\n  ]\n}\n")))
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchHierarchy(
            @Parameter(description = "검색 키워드 (예: 고양, 경상)")
            @RequestParam(name = "q", required = false) String q,
            @Parameter(description = "반환 계층 깊이 (1=광역만, 2=광역+기초). 기본=2")
            @RequestParam(name = "depth", required = false) Integer depth,
            @Parameter(description = "결과 제한 수. 기본=전체")
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        RegionHierarchySearchResponseDto body = regionsService.searchHierarchy(q, depth, limit);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "지역 운행 서비스", description = "특정 지역의 기준 날짜(YYYY-MM-DD)에 운행되는 코스와 정류장 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RegionServiceResponseDto.class),
                            examples = @ExampleObject(name = "RegionServiceExample", value = "{\n  \"region\": { \"regionId\": \"1001\", \"name\": \"고양시\" },\n  \"date\": \"2025-09-04\",\n  \"items\": [\n    {\n      \"courseId\": \"2001\",\n      \"courseName\": \"목요일 09시 코스\",\n      \"stops\": [\n        { \"order\": 1, \"stopId\": \"STP_1001\", \"name\": \"능곡초교\", \"lat\": 37.66301, \"lng\": 126.83201, \"etaToNextSec\": 300, \"dwellSeconds\": 120 },\n        { \"order\": 2, \"stopId\": \"STP_1002\", \"name\": \"정발산\",  \"lat\": 37.65788, \"lng\": 126.85877, \"etaToNextSec\": 300, \"dwellSeconds\": 120 }\n      ]\n    }\n  ]\n}\n"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"code\": \"BAD_REQUEST\", \"message\": \"date 형식은 YYYY-MM-DD 이어야 합니다.\" }"))),
            @ApiResponse(responseCode = "404", description = "찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"code\": \"NOT_FOUND\", \"message\": \"지역을 찾을 수 없습니다.\" }")))
    })
    @GetMapping("/{regionId}/service")
    public ResponseEntity<?> getRegionService(
            @Parameter(description = "지역 ID (내부 숫자 PK 문자열)", required = true)
            @PathVariable("regionId") String regionId,
            @Parameter(description = "기준 날짜 (YYYY-MM-DD)", required = true)
            @RequestParam("date") String date
    ) {
        RegionServiceResponseDto result = regionsService.getRegionService(regionId, date);
        return ResponseEntity.ok(result);
    }
}
