package me.nam.dreamdriversserver.domain.region.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionHierarchySearchResponseDto {
    private List<RegionItem> regions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionItem {
        private String regionId;
        private String name;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<ChildItem> children;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildItem {
        private String regionId;
        private String name;
    }
}

