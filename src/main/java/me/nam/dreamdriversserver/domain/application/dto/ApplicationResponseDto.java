package me.nam.dreamdriversserver.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.nam.dreamdriversserver.domain.application.entity.Applications;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponseDto {

    private Long appId;
    private Long regionId;
    private String regionName;
    private String date;   // yyyy-MM-dd
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