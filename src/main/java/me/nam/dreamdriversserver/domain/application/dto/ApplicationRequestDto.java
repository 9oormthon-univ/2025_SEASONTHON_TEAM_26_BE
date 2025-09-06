package me.nam.dreamdriversserver.domain.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ApplicationRequest", description = "버스 신청 생성 요청 바디")
public class ApplicationRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "신청자 이름", example = "홍길동")
    private String name;

    @Min(value = 0) @Max(value = 120)
    @Schema(description = "나이", example = "67")
    private Integer age;

    @NotBlank
    @Pattern(regexp = "^[0-9\\-+]{9,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @NotBlank
    @Schema(description = "주소", example = "서울시 서초구 서초대로 1")
    private String address;

    // 선택 항목들(없어도 요청 가능하도록)
    @Schema(description = "희망 도서명", example = "파운데이션")
    private String desiredBook;
    @Schema(description = "희망 프로그램", example = "독서 모임")
    private String desiredProgram;

    // 신청 대상
    @NotNull
    @Schema(description = "지역 ID", example = "1001")
    private Long regionId;

    // 서비스 기준 날짜(yyyy-MM-dd). 프론트가 문자열로 보낼 경우 String 유지
    @NotBlank
    @Schema(description = "서비스 날짜(yyyy-MM-dd)", example = "2025-09-10")
    private String date;

    // 신규: 위경도 (선택)
    @Schema(description = "위도", example = "37.4836")
    private Double lat; // null 허용
    @Schema(description = "경도", example = "127.0326")
    private Double lng; // null 허용
}
