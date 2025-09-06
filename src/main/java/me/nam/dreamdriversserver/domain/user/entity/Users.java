package me.nam.dreamdriversserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;
import me.nam.dreamdriversserver.domain.application.entity.Applications;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId; // 사용자 ID, PK

    @Column(name = "login_id", nullable = false, length = 255, unique = true)
    private String loginId; // 사용자 아이디(이메일)

    @Column(name = "password", length = 255)
    private String password; // 비밀번호(해시값)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 사용자 이름

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email; // 사용자 이메일

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 생성일

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<Applications> applications = new ArrayList<>();

    // Users.java 내부
    public static Users ofId(Long userId) {
        return Users.builder()
                .userId(userId) // 필드명이 id가 아니라 userId인 프로젝트 구조에 맞춤
                .build();
    }
}