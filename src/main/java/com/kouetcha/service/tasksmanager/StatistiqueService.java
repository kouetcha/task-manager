package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CountMenuDto;

public interface StatistiqueService {
    CountMenuDto recupereMenuCount(String email);
}
