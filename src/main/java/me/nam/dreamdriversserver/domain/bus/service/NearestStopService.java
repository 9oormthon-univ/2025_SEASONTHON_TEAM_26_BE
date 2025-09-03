package me.nam.dreamdriversserver.domain.bus.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.bus.dto.NearestStopResponseDto;
import me.nam.dreamdriversserver.domain.bus.dto.StopDetailResponseDto;
import me.nam.dreamdriversserver.domain.bus.entity.Stops;
import me.nam.dreamdriversserver.domain.bus.entity.BusLive;
import me.nam.dreamdriversserver.domain.bus.repository.StopsRepository;
import me.nam.dreamdriversserver.domain.bus.repository.BusLiveRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;

/**
 * 가장 가까운 정류장 서비스
 * - 사용자의 위경도만으로 최근접 정류장을 찾고, 해당 정류장의 지역으로 실시간 버스 도착/정차 정보를 함께 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class NearestStopService {
    private final StopsRepository stopsRepository;   // 정류장 위치/거리 기반 조회
    private final BusLiveRepository busLiveRepository; // 실시간 버스 상태 조회

    /**
     * 최근접 정류장 조회
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @return 최근접 정류장 정보 DTO 또는 null(컨트롤러에서 404 처리)
     */
    public NearestStopResponseDto getNearestStop(double lat, double lng) {
        // 1) 지역 제한 없이 최근접 정류장 조회
        Optional<Stops> stopOpt = stopsRepository.findNearestStopAll(lat, lng);
        if (stopOpt.isEmpty()) return null;
        Stops stop = stopOpt.get();

        // 2) 거리 계산(Haversine, 지구 반경 6371000m)
        double dLat = Math.toRadians(stop.getLat().doubleValue() - lat);
        double dLng = Math.toRadians(stop.getLng().doubleValue() - lng);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(stop.getLat().doubleValue())) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        int distanceMeters = (int)(6371000 * c);

        // 3) 해당 정류장의 지역 ID를 사용하여 실시간 버스 정보 조회
        Long regionId = (stop.getRegion() != null) ? stop.getRegion().getRegionId() : null;
        List<BusLive> liveBuses = (regionId != null)
                ? busLiveRepository.findLiveBusesByRegionAndStop(regionId, stop.getStopId())
                : List.of();
        String nextArrivalTime = null;
        int dwellSeconds = 0;
        if (!liveBuses.isEmpty()) {
            BusLive soonest = liveBuses.stream().min((a1, a2) -> Integer.compare(
                    a1.getEtaToNextSec() != null ? a1.getEtaToNextSec() : Integer.MAX_VALUE,
                    a2.getEtaToNextSec() != null ? a2.getEtaToNextSec() : Integer.MAX_VALUE
            )).get();
            nextArrivalTime = soonest.getEtaToNextSec() != null
                    ? java.time.LocalTime.now().plusSeconds(soonest.getEtaToNextSec()).toString()
                    : null;
            dwellSeconds = soonest.getRemainingDwellSec() != null ? soonest.getRemainingDwellSec() : 0;
        }

        // 4) 응답 DTO 생성 (userLat/userLng 포함) + 정류장 좌표 포함
        NearestStopResponseDto dto = new NearestStopResponseDto(
                stop.getStopId(),
                stop.getStopName(),
                nextArrivalTime,
                dwellSeconds,
                distanceMeters,
                lat,
                lng
        );
        dto.setStopLat(stop.getLat() != null ? stop.getLat().doubleValue() : 0.0);
        dto.setStopLng(stop.getLng() != null ? stop.getLng().doubleValue() : 0.0);
        return dto;
    }

    /**
     * 정류장 상세 조회(모달)
     * @param stopId 정류장 ID
     * @return 정류장 상세 정보 DTO 또는 null(존재하지 않는 정류장 ID인 경우)
     */
    public StopDetailResponseDto getStopDetail(Long stopId) {
        Optional<Stops> stopOpt = stopsRepository.findById(stopId);
        if (stopOpt.isEmpty()) return null;
        Stops stop = stopOpt.get();
        Long regionId = (stop.getRegion() != null) ? stop.getRegion().getRegionId() : null;

        List<BusLive> liveBuses = (regionId != null)
                ? busLiveRepository.findLiveBusesByRegionAndStop(regionId, stop.getStopId())
                : List.of();

        String nextArrivalTime = null;
        int dwellSeconds = 0;
        if (!liveBuses.isEmpty()) {
            BusLive soonest = liveBuses.stream().min((a1, a2) -> Integer.compare(
                    a1.getEtaToNextSec() != null ? a1.getEtaToNextSec() : Integer.MAX_VALUE,
                    a2.getEtaToNextSec() != null ? a2.getEtaToNextSec() : Integer.MAX_VALUE
            )).get();
            Integer etaSec = soonest.getEtaToNextSec();
            if (etaSec != null) {
                var nowKst = java.time.ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                var eta = nowKst.plusSeconds(etaSec).toLocalTime();
                nextArrivalTime = eta.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            dwellSeconds = soonest.getRemainingDwellSec() != null ? soonest.getRemainingDwellSec() : 0;
        }

        return new StopDetailResponseDto(
                stop.getStopId(),
                regionId,
                stop.getStopName(),
                nextArrivalTime,
                dwellSeconds
        );
    }
}
