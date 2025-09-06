package me.nam.dreamdriversserver.domain.application.entity;

import jakarta.persistence.*;
import lombok.*;
import me.nam.dreamdriversserver.domain.user.entity.Users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import me.nam.dreamdriversserver.domain.region.entity.Regions;
import java.math.BigDecimal; // 추가
import me.nam.dreamdriversserver.domain.bus.entity.DayOfWeek; // 추가
import me.nam.dreamdriversserver.domain.bus.entity.Stops; // 추가

@Entity
@Getter
@Setter // 신규 필드 세터 필요
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

    // 요일 배정 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_dow", length = 10)
    private DayOfWeek assignedDow;

    @Column(name = "day_seq")
    private Integer daySeq;

    // 위치 정보
    @Column(name = "lat", precision = 11, scale = 8)
    private BigDecimal lat;

    @Column(name = "lng", precision = 11, scale = 8)
    private BigDecimal lng;

    // 최근접 정류장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id")
    private Stops stop;

    @Builder
    private Applications(Long appId, Users user, Regions region, String name, Integer age, String phoneNumber, String address,
                         String desiredBook, String desiredProgram, LocalDate serviceDate, ApplicationStatus status, LocalDateTime createdAt,
                         DayOfWeek assignedDow, Integer daySeq, BigDecimal lat, BigDecimal lng, Stops stop) {
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
        this.assignedDow = assignedDow;
        this.daySeq = daySeq;
        this.lat = lat;
        this.lng = lng;
        this.stop = stop;
    }

    public static Applications ofUserId(Long userId) {
        Users userProxy = Users.builder().userId(userId).build();
        return Applications.builder()
                .user(userProxy)
                .build();
    }

    public void assignDay(DayOfWeek dow, int seq) {
        this.assignedDow = dow;
        this.daySeq = seq;
    }

    public void assignNearestStop(Stops stop) {
        this.stop = stop;
    }
}
