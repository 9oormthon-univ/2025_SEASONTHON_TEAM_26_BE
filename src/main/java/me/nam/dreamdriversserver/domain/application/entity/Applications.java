package me.nam.dreamdriversserver.domain.application.entity;

import jakarta.persistence.*;
import lombok.*;
import me.nam.dreamdriversserver.domain.user.entity.Users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import me.nam.dreamdriversserver.domain.region.entity.Regions;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "applications")
public class Applications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long appId; // 신청 아이디, PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 신청자 (FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Regions region; // 신청 지역 (FK)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 신청자 이름

    @Column(name = "age", nullable = false)
    private Integer age; // 신청자 나이

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber; // 신청자 연락처

    @Column(name = "address", length = 255)
    private String address; // 신청자 주소

    @Column(name = "desired_book", length = 100)
    private String desiredBook; // 희망 도서

    @Column(name = "desired_program", length = 100)
    private String desiredProgram; // 희망 프로그램

    @Column(name = "service_date")
    private LocalDate serviceDate; // 서비스 신청 날짜

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ApplicationStatus status; // 신청 상태

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 신청 생성일

    @Builder
    private Applications(Long appId, Users user, Regions region, String name, Integer age, String phoneNumber, String address,
                         String desiredBook, String desiredProgram, LocalDate serviceDate, ApplicationStatus status, LocalDateTime createdAt) {
        this.appId = appId;
        this.user = user;
        this.region = region;
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.desiredBook = desiredBook;
        this.desiredProgram = desiredProgram;
        this.serviceDate = serviceDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Applications ofUserId(Long userId) {
        Users userProxy = Users.builder().userId(userId).build();
        return Applications.builder()
                .user(userProxy)
                .build();
    }
}
