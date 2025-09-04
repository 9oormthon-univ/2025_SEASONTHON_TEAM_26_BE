package me.nam.dreamdriversserver.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"regionId", "region_name", "date", "capacity", "appliedCount", "remaining", "fillRatePercent"})
public class ApplicationSummaryResponseDto {
    private String regionId;
    @JsonProperty("region_name")
    private String regionName;
    private String date;
    private int capacity;
    private long appliedCount;
    private int remaining;
    private double fillRatePercent;
}
