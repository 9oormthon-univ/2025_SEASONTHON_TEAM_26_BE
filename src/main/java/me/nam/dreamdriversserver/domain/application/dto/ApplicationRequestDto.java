package me.nam.dreamdriversserver.domain.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Min(value = 0) @Max(value = 120)
    private Integer age;

    @NotBlank
    @Pattern(regexp = "^[0-9\\-+]{9,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    @NotBlank
    private String address;

    // 선택 항목들(없어도 요청 가능하도록)
    private String desiredBook;
    private String desiredProgram;

    // 신청 대상
    @NotNull
    private Long regionId;

    // 서비스 기준 날짜(yyyy-MM-dd). 프론트가 문자열로 보낼 경우 String 유지
    @NotBlank
    private String date;
}