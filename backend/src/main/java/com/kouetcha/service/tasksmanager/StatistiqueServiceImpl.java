package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.enums.Type;
import com.kouetcha.repository.tasksmanager.ActiviteRepository;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import com.kouetcha.repository.tasksmanager.TacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService{
    private final ActiviteRepository activiteRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;

    @Override
    public CountMenuDto recupereMenuCount(String email){
        return new CountMenuDto(
                projetRepository.countActiveByEmail(email),
                activiteRepository.countActiveByEmail(email),
                tacheRepository.countActiveByEmail(email)
        );
    }

    @Override
   public DashboardDto getDashboard(Long userId,String email) {
        StatsProjection taches    = tacheRepository.getStatsTaches(userId);
        StatsProjection projets   = projetRepository.getStatsProjets(userId);
        StatsProjection activites = activiteRepository.getStatsActivites(userId);

        DashboardStatsDto stats = DashboardStatsDto.builder()
                .totalTaches(nullSafe(taches.getTotal()))
                .tachesEnCours(nullSafe(taches.getEnCours()))
                .tachesTerminees(nullSafe(taches.getTermines()))
                .tachesEnAttente(nullSafe(taches.getEnAttente()))
                .tachesAnnulees(nullSafe(taches.getAnnules()))

                .totalActivites(nullSafe(activites.getTotal()))
                .activitesEnCours(nullSafe(activites.getEnCours()))
                .activitesTerminees(nullSafe(activites.getTermines()))
                .activitesEnAttente(nullSafe(activites.getEnAttente()))
                .activitesAnnulees(nullSafe(activites.getAnnules()))

                .totalProjets(nullSafe(projets.getTotal()))
                .projetsEnCours(nullSafe(projets.getEnCours()))
                .projetsTermines(nullSafe(projets.getTermines()))
                .projetsEnAttente(nullSafe(projets.getEnAttente()))
                .projetsAnnules(nullSafe(projets.getAnnules()))
                .build();

        return DashboardDto.builder()
                .stats(stats)
                .enRetard(getEnRetard(userId))
                .activiteRecente(getDerniersElements(userId,email))
                .build();
    }

    // Evite les NullPointerException si la table est vide
    private long nullSafe(Long value) {
        return value != null ? value : 0L;
    }

    public List<DashboardItemDto> getEnRetard(Long userId) {
        List<Object[]> rows = new ArrayList<>();
        rows.addAll(projetRepository.findProjetsEnRetard(userId));
        rows.addAll(activiteRepository.findActivitesEnRetard(userId));
        rows.addAll(tacheRepository.findTachesEnRetard(userId));

        return mapDashboard(rows).stream()

                .sorted(Comparator.comparing(
                        DashboardItemDto::getDateFin,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();
    }

    public List<DashboardItemDto> getDerniersElements(Long userId,String email) {
        return mapDashboard(projetRepository.findTopDashboard(userId, email,10));
    }

    public List<DashboardItemDto> mapDashboard(List<Object[]> rows) {
        return rows.stream().map(r -> {
            DashboardItemDto dto = new DashboardItemDto();

            dto.setId(((Number) r[0]).longValue());
            dto.setDesignation((String) r[1]);
            dto.setStatus((String) r[2]);
            dto.setDateDebut(toLocalDateTime(r[3]));
            dto.setDateFin(toLocalDateTime(r[4]));
            dto.setDateModification(toLocalDateTime(r[5]));
            dto.setType(Type.valueOf((String) r[6]));
            dto.setProjetId(r[7] != null ? ((Number) r[7]).longValue() : null);
            dto.setActiviteId(r[8] != null ? ((Number) r[8]).longValue() : null);

            return dto;
        }).toList();
    }


    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof java.sql.Date d) return d.toLocalDate().atStartOfDay();
        if (value instanceof String s) {
            try { return LocalDateTime.parse(s); } catch (Exception e) { return null; }
        }
        return null;
    }
}
