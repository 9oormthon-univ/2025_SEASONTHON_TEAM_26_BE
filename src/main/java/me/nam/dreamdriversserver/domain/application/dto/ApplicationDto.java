package me.nam.dreamdriversserver.domain.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationDto {
    private Long appId;
    private Long userId;
    private Long regionId;
    private String name;
    private Integer age;
    private String phoneNumber;
    private String address;
    private String desiredBook;
    private String desiredProgram;
    private String status;
    private String serviceDate;
    private String createdAt;
}

