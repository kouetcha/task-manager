package com.kouetcha.dto.tasksmanager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarEventDto {
    private String  id;          // "PROJET-12", "ACTIVITE-5", "TACHE-8"
    private String  title;
    private String  start;       // ISO-8601 : "2025-03-01"
    private String  end;         // ISO-8601 : "2025-03-15"
    private String  color;       // couleur de fond FullCalendar
    private String  textColor;
    private boolean allDay;
    private Map<String, Object> extendedProps;
}
