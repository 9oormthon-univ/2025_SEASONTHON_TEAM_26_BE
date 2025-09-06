package me.nam.dreamdriversserver.domain.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.nam.dreamdriversserver.domain.application.entity.Applications;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ApplicationResponse", description = "버스 신청 생성 결과")
public class ApplicationResponseDto {

    @Schema(description = "신청 ID", example = "101")
    private Long appId;
    @Schema(description = "지역 ID", example = "1001")
    private Long regionId;
    @Schema(description = "지역 이름", example = "고양시")
    private String regionName;
    @Schema(description = "서비스 날짜(yyyy-MM-dd)", example = "2025-09-10")
    private String date;   // yyyy-MM-dd
    @Schema(description = "상태", example = "PENDING")
    private String status; // PENDING / CONFIRMED 등

    public static ApplicationResponseDto from(Applications e) {
        if (e == null) return null;

        String dateStr = (e.getServiceDate() != null)
                ? e.getServiceDate().toString()
                : null;

        return ApplicationResponseDto.builder()
                .appId(e.getAppId()) // ← 여기를 getId()에서 변경
                .regionId(e.getRegion() != null ? e.getRegion().getRegionId() : null)
                .regionName(e.getRegion() != null ? e.getRegion().getName() : null)
                .date(dateStr)
                .status(String.valueOf(e.getStatus()))
                .build();
    }
}