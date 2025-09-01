package me.nam.dreamdriversserver.domain.bus.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusDto {
    private Long busId;
    private Long regionId;
    private Long courseId;
    private String updatedAt;
}

