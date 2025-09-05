package me.nam.dreamdriversserver.domain.bus.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.common.exception.NotFoundException;
import me.nam.dreamdriversserver.domain.bus.dto.NearestStopResponseDto;
import me.nam.dreamdriversserver.domain.bus.dto.StopDetailResponseDto;
import me.nam.dreamdriversserver.domain.bus.entity.BusLive;
import me.nam.dreamdriversserver.domain.bus.entity.Stops;
import me.nam.dreamdriversserver.domain.bus.repository.BusLiveRepository;
import me.nam.dreamdriversserver.domain.bus.repository.StopsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NearestStopService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private final StopsRepository stopsRepository;
    private final BusLiveRepository busLiveRepository;

    public NearestStopResponseDto getNearestStop(double lat, double lng) {
        Stops stop = stopsRepository.findNearestStopAll(lat, lng)
                .orElseThrow(() -> new NotFoundException("주변 정류장 없음"));

        var latBD = stop.getLat();
        var lngBD = stop.getLng();
        if (latBD == null || lngBD == null) throw new NotFoundException("정류장 좌표 정보가 없습니다");

        double stopLat = latBD.doubleValue();
        double stopLng = lngBD.doubleValue();
        int distanceMeters = haversineMeters(lat, lng, stopLat, stopLng);

        // ★ region 필터 제거: 현재 모델에는 regionId가 BusLive에 없음
        List<BusLive> liveBuses = busLiveRepository.findByCurrentStopId(stop.getStopId());

        String nextArrivalTime = null;
        int dwellSeconds = 0;
        Integer etaSecFinal = null;
        if (!liveBuses.isEmpty()) {
            BusLive soonest = liveBuses.stream().min(
                    Comparator.comparingInt(bl -> bl.getEtaToNextSec() != null ? bl.getEtaToNextSec() : Integer.MAX_VALUE)
            ).get();

            Integer etaSec = soonest.getEtaToNextSec();
            if (etaSec != null) {
                var nowKst = java.time.ZonedDateTime.now(KST);
                nextArrivalTime = nowKst.plusSeconds(etaSec).toLocalTime().format(HHMM);
                etaSecFinal = etaSec;
            }
            dwellSeconds = soonest.getRemainingDwellSec() != null ? soonest.getRemainingDwellSec() : 0;
        }

        NearestStopResponseDto dto = new NearestStopResponseDto(
                stop.getStopId(), stop.getStopName(), nextArrivalTime, dwellSeconds, distanceMeters, lat, lng
        );
        dto.setStopLat(stopLat);
        dto.setStopLng(stopLng);
        // region 정보
        if (stop.getRegion() != null) {
            dto.setRegionId(stop.getRegion().getRegionId());
            dto.setRegionName(stop.getRegion().getName());
        }
        // ETA(초)
        dto.setEtaToNextSec(etaSecFinal);
        return dto;
    }

    public StopDetailResponseDto getStopDetail(Long stopId) {
        Stops stop = stopsRepository.findById(stopId)
                .orElseThrow(() -> new NotFoundException("정류장 없음"));

        // 실시간 버스 상태
        List<BusLive> liveBuses = busLiveRepository.findByCurrentStopId(stop.getStopId());

        int dwellSeconds = 0;
        Integer etaSecFinal = null;
        if (!liveBuses.isEmpty()) {
            BusLive soonest = liveBuses.stream().min(
                    Comparator.comparingInt(bl -> bl.getEtaToNextSec() != null ? bl.getEtaToNextSec() : Integer.MAX_VALUE)
            ).get();
            Integer etaSec = soonest.getEtaToNextSec();
            if (etaSec != null) {
                etaSecFinal = etaSec;
            }
            dwellSeconds = soonest.getRemainingDwellSec() != null ? soonest.getRemainingDwellSec() : 0;
        }

        Long regionId = (stop.getRegion() != null) ? stop.getRegion().getRegionId() : null;
        String regionName = (stop.getRegion() != null) ? stop.getRegion().getName() : null;
        double stopLat = stop.getLat() != null ? stop.getLat().doubleValue() : 0.0;
        double stopLng = stop.getLng() != null ? stop.getLng().doubleValue() : 0.0;

        return new StopDetailResponseDto(
                stop.getStopId(),
                regionId,
                stop.getStopName(),
                // nextArrivalTime 제거됨 → dwellSeconds가 바로 옴
                dwellSeconds,
                regionName,
                etaSecFinal,
                0,
                stopLat,
                stopLng
        );
    }

    /** 상세 조회: 사용자 좌표를 받아 distanceMeters와 ETA초를 포함해 반환 */
    public StopDetailResponseDto getStopDetail(Long stopId, Double userLat, Double userLng) {
        Stops stop = stopsRepository.findById(stopId)
                .orElseThrow(() -> new NotFoundException("정류장 없음"));

        double stopLat = stop.getLat() != null ? stop.getLat().doubleValue() : 0.0;
        double stopLng = stop.getLng() != null ? stop.getLng().doubleValue() : 0.0;

        int distanceMeters = 0;
        if (userLat != null && userLng != null) {
            distanceMeters = haversineMeters(userLat, userLng, stopLat, stopLng);
        }

        List<BusLive> liveBuses = busLiveRepository.findByCurrentStopId(stop.getStopId());

        int dwellSeconds = 0;
        Integer etaSecFinal = null;
        if (!liveBuses.isEmpty()) {
            BusLive soonest = liveBuses.stream().min(
                    Comparator.comparingInt(bl -> bl.getEtaToNextSec() != null ? bl.getEtaToNextSec() : Integer.MAX_VALUE)
            ).get();
            Integer etaSec = soonest.getEtaToNextSec();
            if (etaSec != null) {
                etaSecFinal = etaSec;
            }
            dwellSeconds = soonest.getRemainingDwellSec() != null ? soonest.getRemainingDwellSec() : 0;
        }

        Long regionId = (stop.getRegion() != null) ? stop.getRegion().getRegionId() : null;
        String regionName = (stop.getRegion() != null) ? stop.getRegion().getName() : null;

        return new StopDetailResponseDto(
                stop.getStopId(),
                regionId,
                stop.getStopName(),
                dwellSeconds,
                regionName,
                etaSecFinal,
                distanceMeters,
                stopLat,
                stopLng
        );
    }

    private static int haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (int)(6371000 * c);
    }
}