package me.nam.dreamdriversserver.domain.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 신청(Application) 조회/응답에 사용되는 DTO
 * - 엔티티를 직접 노출하지 않고, 표현 계층에 필요한 필드만 전달하기 위한 용도
 */
@Getter
@Setter
public class ApplicationDto {
    /** 신청 ID(PK) */
    private Long appId;
    /** 신청자 사용자 ID(FK: users.user_id) */
    private Long userId;
    /** 신청 지역 ID(FK: regions.region_id) */
    private Long regionId;
    /** 신청자 이름 */
    private String name;
    /** 신청자 나이 */
    private Integer age;
    /** 신청자 연락처 */
    private String phoneNumber;
    /** 신청자 주소 */
    private String address;
    /** 희망 도서 */
    private String desiredBook;
    /** 희망 프로그램 */
    private String desiredProgram;
    /** 신청 상태(PENDING/CONFIRMED/CANCELLED) */
    private String status;
    /** 서비스 신청 일자(YYYY-MM-DD) */
    private String serviceDate;
    /** 신청 생성일(ISO-8601 문자열) */
    private String createdAt;
}
