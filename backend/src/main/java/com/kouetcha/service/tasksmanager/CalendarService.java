package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CalendarEventDto;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<CalendarEventDto> getCalendarEvents(
            Long userId, String userEmail, LocalDate start, LocalDate end);
}
