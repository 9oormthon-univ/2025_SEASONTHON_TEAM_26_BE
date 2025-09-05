package me.nam.dreamdriversserver.domain.bus.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.bus.dto.BusLiveResponseDto;
import me.nam.dreamdriversserver.domain.bus.entity.*;
import me.nam.dreamdriversserver.domain.bus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BusLiveService {

    private final CourseRepository courseRepository;
    private final CourseStopsRepository courseStopsRepository;
    private final BusRepository busRepository;
    private final BusLiveRepository busLiveRepository;

    // TIME_FMT 제거: plannedArrival 필드를 더 이상 사용하지 않음
    private static final DateTimeFormatter ISO_OFFSET_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Transactional(readOnly = true)
    public BusLiveResponseDto getCourseLive(String courseIdPath, String tz) {
        ZoneId zoneId = (tz == null || tz.isBlank()) ? ZoneId.of("Asia/Seoul") : ZoneId.of(tz);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        // 가정: path의 courseId는 Long 기반의 내부 PK를 문자열로 전달
        Long courseId;
        try { courseId = Long.valueOf(courseIdPath); }
        catch (Exception e) { throw notFound("코스를 찾을 수 없음"); }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> notFound("코스를 찾을 수 없음"));

        // 코스에 매핑된 버스 목록 조회
        List<Buses> buses = busRepository.findByCourse_CourseId(course.getCourseId());
        if (buses.isEmpty()) throw notFound("코스-버스 매핑 없음");

        // 버스별 최신 BusLive 선택 (가장 최신 updatedAt)
        BusLive latest = null;
        for (Buses b : buses) {
            Optional<BusLive> snap = busLiveRepository.findTopByBus_BusIdOrderByUpdatedAtDesc(b.getBusId());
            if (snap.isEmpty()) continue;
            if (latest == null || (latest.getUpdatedAt() != null && snap.get().getUpdatedAt() != null
                    && snap.get().getUpdatedAt().isAfter(latest.getUpdatedAt()))
                    || (latest.getUpdatedAt() == null && snap.get().getUpdatedAt() != null)) {
                latest = snap.get();
            }
        }
        if (latest == null) throw notFound("실시간 운행 정보 없음");

        // 코스 정류장 목록(order ASC)
        List<CourseStops> courseStops = courseStopsRepository.findByCourseIdOrderByStopOrder(course.getCourseId());

        // 인덱스 맵: stopId -> order
        Map<Long, Integer> stopOrderMap = new HashMap<>();
        for (CourseStops cs : courseStops) {
            if (cs.getStop() != null && cs.getStop().getStopId() != null) {
                stopOrderMap.put(cs.getStop().getStopId(), cs.getStopOrder());
            }
        }

        // 상태 판정 입력값
        Integer srcEtaToNext = latest.getEtaToNextSec(); // 이제 "도착시간" 의미로 그대로 사용
        Integer srcRemainDwell = latest.getRemainingDwellSec();
        Long currentStopId = latest.getCurrentStopId();
        Long nextStopId = latest.getNextStopId();
        LocalDateTime srcUpdatedAt = latest.getUpdatedAt();
        long elapsed = 0L;
        if (srcUpdatedAt != null) {
            elapsed = Duration.between(srcUpdatedAt.atZone(zoneId), now).getSeconds();
            if (elapsed < 0) elapsed = 0;
        }

        // 상태 판정
        String status;
        Integer dwellSeconds = null;
        Integer etaToNextSec = null;

        boolean isStopped = (srcRemainDwell != null && srcRemainDwell > 0 && currentStopId != null);
        boolean isMoving = (srcEtaToNext != null && srcEtaToNext > 0 && (srcRemainDwell == null || srcRemainDwell <= 0));

        if (isStopped) {
            status = "STOPPED";
            dwellSeconds = Math.max(0, srcRemainDwell - (int) elapsed);
            etaToNextSec = srcEtaToNext; // 감소하지 않음(도착시간 값 반환)
        } else if (isMoving) {
            status = "IN_SERVICE";
            dwellSeconds = null;
            etaToNextSec = srcEtaToNext; // 감소하지 않음(도착시간 값 반환)
        } else {
            status = "OFFLINE";
            dwellSeconds = null;
            etaToNextSec = null; // OFFLINE은 null
        }

        // progress 계산
        Integer currentOrder = null;
        Integer nextOrder = null;
        if ("STOPPED".equals(status)) {
            if (currentStopId != null && stopOrderMap.containsKey(currentStopId)) {
                currentOrder = stopOrderMap.get(currentStopId);
                nextOrder = (currentOrder != null) ? currentOrder + 1 : null;
            }
        } else if ("IN_SERVICE".equals(status)) {
            if (nextStopId != null && stopOrderMap.containsKey(nextStopId)) {
                nextOrder = stopOrderMap.get(nextStopId);
                currentOrder = (nextOrder != null) ? nextOrder - 1 : null;
            }
        } else {
            // OFFLINE
            currentOrder = null;
            nextOrder = null;
        }

        // stops 배열 구성
        List<BusLiveResponseDto.Stop> stops = new ArrayList<>();
        int lastOrder = 0;
        for (CourseStops cs : courseStops) {
            if (cs.getStop() == null) continue;
            Stops s = cs.getStop();
            Integer order = cs.getStopOrder();
            lastOrder = Math.max(lastOrder, order == null ? 0 : order);
            Integer dwellRemainingSec = null;
            Integer etaFromNowSec = null; // 이름은 유지하되 의미는 "���착시간"으로 사용

            boolean isCurrent = (currentOrder != null && order != null && Objects.equals(order, currentOrder));
            boolean isNext = (nextOrder != null && order != null && Objects.equals(order, nextOrder));

            if ("STOPPED".equals(status)) {
                if (isCurrent) {
                    dwellRemainingSec = dwellSeconds; // 감소
                } else if (isNext) {
                    etaFromNowSec = srcEtaToNext; // 도착시간 값 그대로
                }
            } else if ("IN_SERVICE".equals(status)) {
                if (isNext) {
                    etaFromNowSec = srcEtaToNext; // 도착시간 값 그대로(감소 없음)
                }
            } else {
                // OFFLINE -> 모두 null
            }

            BusLiveResponseDto.Stop dtoStop = BusLiveResponseDto.Stop.builder()
                    .order(order)
                    .stopId(String.valueOf(s.getStopId()))
                    .name(s.getStopName())
                    .position(new BusLiveResponseDto.Position(
                            s.getLat() == null ? null : s.getLat().doubleValue(),
                            s.getLng() == null ? null : s.getLng().doubleValue()
                    ))
                    .live(BusLiveResponseDto.Live.builder()
                            .dwellRemainingSec(dwellRemainingSec)
                            .etaFromNowSec(etaFromNowSec)
                            .build())
                    .build();
            stops.add(dtoStop);
        }

        // nextOrder 존재 검증 (리스트 범위를 벗어나면 null 처리)
        if (nextOrder != null && (nextOrder < 1 || nextOrder > lastOrder)) {
            nextOrder = null;
        }

        // response 조립
        String sourceUpdatedAt = (srcUpdatedAt == null) ? null : ISO_OFFSET_FMT.format(srcUpdatedAt.atZone(zoneId));
        Integer cacheAgeSec = null;
        if (srcUpdatedAt != null) {
            cacheAgeSec = (int) Math.max(0, Duration.between(srcUpdatedAt.atZone(zoneId), now).getSeconds());
        }

        return BusLiveResponseDto.builder()
                .courseId(String.valueOf(course.getCourseId()))
                .serverTime(ISO_OFFSET_FMT.format(now))
                .status(status)
                .progress(new BusLiveResponseDto.Progress(currentOrder, nextOrder))
                .etaToNextSec(etaToNextSec)
                .dwellSeconds(dwellSeconds)
                .stops(stops)
                .sourceUpdatedAt(sourceUpdatedAt)
                .cacheAgeSec(cacheAgeSec)
                .ttlSec(30)
                .build();
    }

    private RuntimeException notFound(String message) {
        return new NoSuchElementException(message);
    }
}
