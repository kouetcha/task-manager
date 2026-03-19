package com.kouetcha.dto.tasksmanager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {

    // Projets
    private long totalProjets;
    private long projetsEnCours;
    private long projetsTermines;
    private long projetsEnAttente;
    private long projetsAnnules;

    // Activités
    private long totalActivites;
    private long activitesEnCours;
    private long activitesTerminees;
    private long activitesEnAttente;
    private long activitesAnnulees;

    // Tâches
    private long totalTaches;
    private long tachesEnCours;
    private long tachesTerminees;
    private long tachesEnAttente;
    private long tachesAnnulees;
}
