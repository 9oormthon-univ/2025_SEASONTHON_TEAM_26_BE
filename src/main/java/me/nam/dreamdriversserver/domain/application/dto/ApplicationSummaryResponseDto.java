package me.nam.dreamdriversserver.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"regionId", "region_name", "date", "capacity", "appliedCount", "remaining", "fillRatePercent"})
@Schema(name = "ApplicationSummaryResponse", description = "신청 현황 요약 응답")
public class ApplicationSummaryResponseDto {
    @Schema(description = "지역 ID", example = "1001")
    private String regionId;
    @JsonProperty("region_name")
    @Schema(description = "지역 이름", example = "고양시")
    private String regionName;
    @Schema(description = "기준 날짜(yyyy-MM-dd)", example = "2025-09-10")
    private String date;
    @Schema(description = "총 정원", example = "40")
    private int capacity;
    @Schema(description = "신청 인원", example = "28")
    private long appliedCount;
    @Schema(description = "잔여 좌석", example = "12")
    private int remaining;
    @Schema(description = "충족률(%)", example = "70.0")
    private double fillRatePercent;
}
