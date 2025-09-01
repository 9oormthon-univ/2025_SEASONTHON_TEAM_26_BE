package me.nam.dreamdriversserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_rt_login_id", columnList = "loginId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String loginId;

    @Column(nullable = false, length = 500)
    private String token;
}