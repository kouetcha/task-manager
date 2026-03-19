package com.kouetcha.dto.tasksmanager;

public interface StatsProjection {
    Long getTotal();
    Long getEnCours();
    Long getTermines();
    Long getEnAttente();
    Long getAnnules();
}
