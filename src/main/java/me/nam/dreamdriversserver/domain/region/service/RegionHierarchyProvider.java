package me.nam.dreamdriversserver.domain.region.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 시/도 → 시·군·구 계층 매핑을 로드/제공하는 Provider.
 * - resources/regions.json 에서 매핑을 로드
 */
@Component
public class RegionHierarchyProvider {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, List<String>> hierarchy = new LinkedHashMap<>();

    @PostConstruct
    public void load() throws IOException {
        // 변경: 리소스 경로를 루트의 regions.json으로 설정
        ClassPathResource resource = new ClassPathResource("regions.json");
        if (!resource.exists()) {
            hierarchy = defaultSidoOnly();
            return;
        }
        try (InputStream is = resource.getInputStream()) {
            Map<String, List<String>> map = objectMapper.readValue(is, new TypeReference<>() {});
            hierarchy = new LinkedHashMap<>(map);
        }
    }

    /** 전체 시/도 키 집합 */
    public Set<String> listSido() {
        return hierarchy.keySet();
    }

    /** 특정 시/도의 자식 목록(시·군·구) */
    public List<String> listChildrenNames(String sido) {
        if (sido == null) return Collections.emptyList();
        return hierarchy.getOrDefault(sido, Collections.emptyList());
    }

    /** 전체 계층 맵 반환 */
    public Map<String, List<String>> getHierarchy() {
        return Collections.unmodifiableMap(hierarchy);
    }

    private Map<String, List<String>> defaultSidoOnly() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("서울특별시", Collections.emptyList());
        map.put("부산광역시", Collections.emptyList());
        map.put("대구광역시", Collections.emptyList());
        map.put("인천광역시", Collections.emptyList());
        map.put("광주광역시", Collections.emptyList());
        map.put("대전광역시", Collections.emptyList());
        map.put("울산광역시", Collections.emptyList());
        map.put("세종특별자치시", Collections.emptyList());
        map.put("경기도", Collections.emptyList());
        map.put("강원특별자치도", Collections.emptyList());
        map.put("충청북도", Collections.emptyList());
        map.put("충청남도", Collections.emptyList());
        map.put("전북특별자치도", Collections.emptyList());
        map.put("전라남도", Collections.emptyList());
        map.put("경상북도", Collections.emptyList());
        map.put("경상남도", Collections.emptyList());
        map.put("제주특별자치도", Collections.emptyList());
        return map;
    }
}
