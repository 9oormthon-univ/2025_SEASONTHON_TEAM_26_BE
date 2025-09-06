package me.nam.dreamdriversserver.domain.bus.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusLiveResponseDto {
    private String courseId;              // 노출용: 현재 구현에서는 Long ID를 문자열로 변환하여 사용
    private String serverTime;           // ISO_OFFSET_DATE_TIME 문자열
    private String status;               // STOPPED | IN_SERVICE | OFFLINE
    private Progress progress;           // 진행 상황
    private Integer etaToNextSec;        // 다음 정류장 도착시간(초)
    private Integer dwellSeconds;        // 정차 남은 시간 (STOPPED일 때만 감소)
    private List<Stop> stops;            // 정류장 리스트
    private String sourceUpdatedAt;      // 데이터 소스 스냅샷 시각 (선택)
    private Integer cacheAgeSec;         // serverTime - sourceUpdatedAt (초)
    private Integer ttlSec;              // 캐시 권장 주기(초)

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Progress {
        private Integer currentOrder;    // 현재 정류장 order (없으면 null)
        private Integer nextOrder;       // 다음 목적지 order (없으면 null)
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stop {
        private Integer order;
        private String stopId;
        private String name;
        private Position position;
        private Live live;               // 실시간 정보
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private Double lat;
        private Double lng;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Live {
        private Integer dwellRemainingSec; // 현재 정류장일 때만, 감소
        private Integer etaFromNowSec;     // 정류장 도착시간(초)
    }
}
