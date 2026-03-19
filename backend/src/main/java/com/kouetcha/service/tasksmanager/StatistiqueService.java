package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CountMenuDto;
import com.kouetcha.dto.tasksmanager.DashboardDto;

public interface StatistiqueService {
    CountMenuDto recupereMenuCount(String email);

    DashboardDto getDashboard(Long userId,String email);
}
