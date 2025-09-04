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
    private String nextArrivalTime; // HH:mm 또는 null
    private int dwellSeconds;       // 정차시간(초)
}

