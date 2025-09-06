package me.nam.dreamdriversserver.domain.region.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionServiceResponseDto {
    private RegionMeta region;
    private String date; // YYYY-MM-DD
    private List<CourseItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionMeta {
        private String regionId; // 문자로 반환 (내부 Long이면 문자열화)
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseItem {
        private String courseId; // 문자열로 반환 (내부 Long이면 문자열화)
        private String courseName; // 요일/시간 포함한 표시용 이름
        private List<StopItem> stops;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopItem {
        private int order; // 1부터 시작
        private String stopId;
        private String name;
        private double lat;
        private double lng;
        private int etaToNextSec;
        private int dwellSeconds;
        // 신규 필드
        private String arrivalTime;     // HH:mm
        private String departureTime;   // HH:mm
        private Long applicantCount;    // 해당 정류장 신청자 수
    }
}
