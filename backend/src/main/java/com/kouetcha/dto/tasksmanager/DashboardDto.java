package com.kouetcha.dto.tasksmanager;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class DashboardDto {
    private DashboardStatsDto stats;
    private List<DashboardItemDto> enRetard;
    private List<DashboardItemDto> activiteRecente;
}
