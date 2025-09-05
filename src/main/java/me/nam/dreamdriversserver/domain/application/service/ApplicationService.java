package me.nam.dreamdriversserver.domain.application.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.application.dto.ApplicationRequestDto;
import me.nam.dreamdriversserver.domain.application.dto.ApplicationResponseDto;
import me.nam.dreamdriversserver.domain.application.entity.Applications;
import me.nam.dreamdriversserver.domain.application.entity.ApplicationStatus;
import me.nam.dreamdriversserver.domain.application.repository.ApplicationsRepository;
import me.nam.dreamdriversserver.domain.region.entity.Regions;
import me.nam.dreamdriversserver.domain.region.repository.RegionsRepository;
import me.nam.dreamdriversserver.domain.user.entity.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationsRepository applicationsRepository;
    private final RegionsRepository regionsRepository;

    @Transactional
    public ApplicationResponseDto create(ApplicationRequestDto req, Long userId) {

        // Region 로드 (존재 검증)
        Regions region = regionsRepository.findById(req.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + req.getRegionId()));

        // Users 레퍼런스(프록시) — 엔티티 로딩 없이 FK만 설정
        Users userRef = Users.ofId(userId); // 👈 아래 Users.ofId 참고

        Applications entity = Applications.builder()
                .user(userRef)
                .region(region)
                .name(req.getName())
                .age(req.getAge())
                .phoneNumber(req.getPhoneNumber())
                .address(req.getAddress())
                .desiredBook(req.getDesiredBook())
                .desiredProgram(req.getDesiredProgram())
                .serviceDate(LocalDate.parse(req.getDate())) // yyyy-MM-dd
                .status(ApplicationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Applications saved = applicationsRepository.save(entity);
        return ApplicationResponseDto.from(saved);
    }
}