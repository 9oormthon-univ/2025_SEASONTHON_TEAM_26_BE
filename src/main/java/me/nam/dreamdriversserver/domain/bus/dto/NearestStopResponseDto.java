package me.nam.dreamdriversserver.domain.bus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GET /stops/nearest 200 응답 DTO
 * - 사용자 위치(lat,lng)와 regionId를 기준으로, 가장 가까운 정류장 정보를 반환합니다.
 * - 404의 경우 이 DTO가 아닌 { code, message } 에러 바디를 반환합니다.
 *
 * 반환 예시(JSON):
 * {
 *   "stopId": 123,
 *   "name": "서초동도서관",
 *   "nextArrivalTime": "09:25",
 *   "dwellSeconds": 120,
 *   "distanceMeters": 184,
 *   "userLat": 37.4921,
 *   "userLng": 127.0305,
 *   "stopLat": 37.4836,
 *   "stopLng": 127.0326
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NearestStopResponseDto {
    /**
     * 정류장 고유 ID(PK)
     * - DB Stops.stop_id와 매핑됩니다.
     */
    private Long stopId;

    /**
     * 정류장 이름
     * - 예: "서초동도서관"
     */
    private String name;

    /**
     * 가장 빠른 버스의 예상 도착 시각(문자열)
     * - 형식: HH:mm (24시간제), Asia/Seoul 타임존 기준
     * - 실시간 정보가 없거나 ETA를 산정할 수 없으면 null
     */
    private String nextArrivalTime;

    /**
     * 해당 정류장에서 남은 정차 시간(초)
     * - 실시간 버스가 정류장에 정차 중일 때 양수, 그렇지 않으면 0
     */
    private int dwellSeconds;

    /**
     * 사용자 현재 위치로부터 정류장까지의 거리(미터)
     * - Haversine 공식으로 계산한 직선 거리
     * - 소수점 이하는 절삭/반올림 전략에 따라 정수 변환됨(현재 구현은 절삭)
     */
    private int distanceMeters;

    /**
     * 요청 사용자 위도
     */
    private double userLat;

    /**
     * 요청 사용자 경도
     */
    private double userLng;

    /**
     * 정류장 위도
     */
    private double stopLat;

    /**
     * 정류장 경도
     */
    private double stopLng;

    public NearestStopResponseDto(Long stopId, String stopName, String nextArrivalTime, int dwellSeconds, int distanceMeters, double lat, double lng) {
        this.stopId = stopId;
        this.name = stopName;
        this.nextArrivalTime = nextArrivalTime;
        this.dwellSeconds = dwellSeconds;
        this.distanceMeters = distanceMeters;
        this.userLat = lat;
        this.userLng = lng;
        // stopLat/stopLng는 별도 설정 시 사용(기본값 0.0)
    }
}
