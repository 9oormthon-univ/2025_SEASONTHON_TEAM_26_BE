package me.nam.dreamdriversserver.domain.region.service;

import lombok.RequiredArgsConstructor;
import me.nam.dreamdriversserver.domain.region.dto.RegionServiceResponseDto;
import me.nam.dreamdriversserver.domain.region.entity.Regions;
import me.nam.dreamdriversserver.domain.region.repository.RegionsRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDate;
import java.util.zip.CRC32;
import me.nam.dreamdriversserver.domain.bus.entity.Course;
import me.nam.dreamdriversserver.domain.bus.entity.CourseStops;
import me.nam.dreamdriversserver.domain.bus.entity.DayOfWeek;
import me.nam.dreamdriversserver.domain.bus.repository.CourseRepository;
import me.nam.dreamdriversserver.domain.bus.repository.CourseStopsRepository;
import me.nam.dreamdriversserver.domain.region.dto.RegionHierarchySearchResponseDto;

@Service
@RequiredArgsConstructor
public class RegionsService {
    private final RegionsRepository regionsRepository;
    private final RegionHierarchyProvider hierarchyProvider;
    private final CourseRepository courseRepository;
    private final CourseStopsRepository courseStopsRepository;

    /** 지역 계층 검색 (q, depth, limit) */
    public RegionHierarchySearchResponseDto searchHierarchy(String q, Integer depth, Integer limit) {
        int d = (depth == null || depth < 1 || depth > 2) ? 2 : depth;
        Integer lim = (limit != null && limit > 0) ? limit : null; // null이면 무제한
        String keyword = q == null ? null : q.trim();
        boolean hasQuery = keyword != null && !keyword.isEmpty();

        Map<String, List<String>> map = hierarchyProvider.getHierarchy();
        List<RegionHierarchySearchResponseDto.RegionItem> result = new ArrayList<>();

        for (Map.Entry<String, List<String>> e : map.entrySet()) {
            String parent = e.getKey();
            List<String> children = e.getValue();
            boolean parentMatch = hasQuery && containsIgnoreCase(parent, keyword);
            List<String> filteredChildren;
            if (hasQuery) {
                filteredChildren = children.stream()
                        .filter(c -> containsIgnoreCase(c, keyword))
                        .toList();
            } else {
                filteredChildren = children;
            }

            boolean include = !hasQuery || parentMatch || !filteredChildren.isEmpty();
            if (!include) continue;

            RegionHierarchySearchResponseDto.RegionItem item = new RegionHierarchySearchResponseDto.RegionItem();
            item.setRegionId(genParentId(parent));
            item.setName(parent);
            if (d == 2) {
                List<RegionHierarchySearchResponseDto.ChildItem> childItems = new ArrayList<>();
                for (String c : filteredChildren) {
                    childItems.add(new RegionHierarchySearchResponseDto.ChildItem(genChildId(parent, c), c));
                }
                item.setChildren(childItems);
            } else {
                item.setChildren(null);
            }
            result.add(item);
            if (lim != null && result.size() >= lim) break;
        }
        return new RegionHierarchySearchResponseDto(result);
    }

    private boolean containsIgnoreCase(String src, String kw) {
        return src != null && kw != null && src.toLowerCase().contains(kw.toLowerCase());
    }

    private String genParentId(String name) {
        return "RGN_" + crc32Hex(name);
        }

    private String genChildId(String parent, String child) {
        return "RGN_" + crc32Hex(parent) + "_" + crc32Hex(child);
    }

    private String crc32Hex(String s) {
        CRC32 crc = new CRC32();
        crc.update(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        long v = crc.getValue();
        return String.format("%08X", v);
    }

    // 문자열 파라미터 처리: 컨트롤러는 검증하지 않고 그대로 위임
    public RegionServiceResponseDto getRegionService(String regionId, String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "date 파라미터는 필수입니다.");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "date 형식은 YYYY-MM-DD 이어야 합니다.");
        }

        long regionPk;
        try {
            regionPk = Long.parseLong(regionId);
        } catch (NumberFormatException e) {
            // 잘못된 regionId 포맷 → 404 처리
            throw new AppException(ErrorCode.NOT_FOUND, "지역을 찾을 수 없습니다.");
        }

        return getRegionService(regionPk, date);
    }

    /** 지역 서비스 정보 조회 (DB 조회) */
    public RegionServiceResponseDto getRegionService(Long regionId, LocalDate date) {
        Optional<Regions> regionOpt = regionsRepository.findById(regionId);
        if (regionOpt.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND, "지역을 찾을 수 없습니다.");
        }
        Regions region = regionOpt.get();

        DayOfWeek dow = mapToDomainDayOfWeek(date);
        if (dow == null) {
            // 주말은 운행 없음: 빈 items로 응답
            return new RegionServiceResponseDto(
                    new RegionServiceResponseDto.RegionMeta(String.valueOf(region.getRegionId()), region.getName()),
                    date.toString(),
                    Collections.emptyList()
            );
        }

        List<Course> courses = courseRepository.findAllActiveByRegionAndDay(regionId, dow);
        List<RegionServiceResponseDto.CourseItem> items = new ArrayList<>();
        for (Course c : courses) {
            List<CourseStops> stops = courseStopsRepository.findByCourseIdOrderByStopOrder(c.getCourseId());
            List<RegionServiceResponseDto.StopItem> stopItems = new ArrayList<>();

            int dwellSec = (c.getDwellMin() != null ? c.getDwellMin() : 30) * 60;
            int travelSec = (c.getTravelMin() != null ? c.getTravelMin() : 30) * 60;

            for (int i = 0; i < stops.size(); i++) {
                CourseStops cs = stops.get(i);
                var s = cs.getStop();
                int order = cs.getStopOrder() != null ? cs.getStopOrder() : (i + 1);
                int etaNext = (i == stops.size() - 1) ? 0 : travelSec;
                stopItems.add(new RegionServiceResponseDto.StopItem(
                        order,
                        String.valueOf(s.getStopId()),
                        s.getStopName(),
                        s.getLat() != null ? s.getLat().doubleValue() : 0.0,
                        s.getLng() != null ? s.getLng().doubleValue() : 0.0,
                        etaNext,
                        dwellSec
                ));
            }

            items.add(new RegionServiceResponseDto.CourseItem(
                    String.valueOf(c.getCourseId()),
                    buildCourseName(c),
                    stopItems
            ));
        }

        return new RegionServiceResponseDto(
                new RegionServiceResponseDto.RegionMeta(String.valueOf(region.getRegionId()), region.getName()),
                date.toString(),
                items
        );
    }

    private DayOfWeek mapToDomainDayOfWeek(LocalDate date) {
        java.time.DayOfWeek j = date.getDayOfWeek();
        return switch (j) {
            case MONDAY -> DayOfWeek.MON;
            case TUESDAY -> DayOfWeek.TUE;
            case WEDNESDAY -> DayOfWeek.WED;
            case THURSDAY -> DayOfWeek.THU;
            case FRIDAY -> DayOfWeek.FRI;
            default -> null; // 주말
        };
    }

    private String buildCourseName(Course c) {
        String dowKo = switch (c.getDow()) {
            case MON -> "월요일";
            case TUE -> "화요일";
            case WED -> "수요일";
            case THU -> "목요일";
            case FRI -> "금요일";
        };
        String hh = c.getStartTime() != null ? String.format("%02d", c.getStartTime().getHour()) : "--";
        return dowKo + " " + hh + "시 코스";
    }
}
