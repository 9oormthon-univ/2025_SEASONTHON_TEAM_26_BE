package me.nam.dreamdriversserver.domain.bus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopDetailResponseDto {
    private Long stopId;
    private Long regionId;
    private String name;
    private int dwellSeconds;       // 정차시간(초)


    // 신규/유지 필드
    private String regionName;      // 지역명
    private Integer etaToNextSec;   // 다음 정류장까지 ETA(초), null 가능
    private int distanceMeters;     // 사용자 기준 거리(미터)
    private double stopLat;         // 정류장 위도
    private double stopLng;         // 정류장 경도
    // 신규 필드
    private String arrivalTime;          // 예정 도착 HH:mm
    private String departureTime;        // 예정 출발 HH:mm
    private Integer dwellPlannedSeconds; // 예정 정차(초)
    private Long applicantCount;         // 해당 정류장 신청자 수

    // 이전 호환 생성자 (신규 필드 null 기본값)
    public StopDetailResponseDto(Long stopId, Long regionId, String name, int dwellSeconds,
                                 String regionName, Integer etaToNextSec, int distanceMeters,
                                 double stopLat, double stopLng) {
        this.stopId = stopId;
        this.regionId = regionId;
        this.name = name;
        this.dwellSeconds = dwellSeconds;
        this.regionName = regionName;
        this.etaToNextSec = etaToNextSec;
        this.distanceMeters = distanceMeters;
        this.stopLat = stopLat;
        this.stopLng = stopLng;
        this.arrivalTime = null;
        this.departureTime = null;
        this.dwellPlannedSeconds = null;
        this.applicantCount = null;
    }
}
