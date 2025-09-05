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
}

