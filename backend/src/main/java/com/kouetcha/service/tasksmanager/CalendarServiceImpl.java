package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.CalendarEventDto;
import com.kouetcha.repository.tasksmanager.ProjetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final ProjetRepository projetRepository;

    // ── Couleurs par type ──────────────────────────────────────────
    private static final Map<String, String> TYPE_COLORS = Map.of(
            "PROJET",   "#3B82F6",   // bleu
            "ACTIVITE", "#10B981",   // vert
            "TACHE",    "#F59E0B"    // orange
    );

    // ── Opacité réduite pour les statuts "fermés" ─────────────────
    private static final Set<String> MUTED_STATUTS = Set.of("TERMINE", "ANNULE");
    @Override
    public List<CalendarEventDto> getCalendarEvents(
            Long userId, String userEmail, LocalDate start, LocalDate end) {

        return projetRepository
                .findCalendarEvents(userId,userEmail, start, end)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private CalendarEventDto mapToDto(Object[] row) {
        String type      = (String) row[5];
        String status    = (String) row[2];
        Long   id        = ((Number) row[0]).longValue();
        Long   projetId  = row[6] != null ? ((Number) row[6]).longValue() : null;
        Long   activiteId= row[7] != null ? ((Number) row[7]).longValue() : null;

        String baseColor = TYPE_COLORS.getOrDefault(type, "#6B7280");
        String color     = MUTED_STATUTS.contains(status)
                ? baseColor + "80"   // 50% opacité via hex alpha
                : baseColor;

        // FullCalendar : end est EXCLUSIF → on ajoute 1 jour
        LocalDate dateFin = toLocalDate(row[4]);
        String endExclusive = dateFin != null
                ? dateFin.plusDays(1).toString()
                : null;

        Map<String, Object> props = new HashMap<>();
        props.put("type",       type);
        props.put("id",       id);
        props.put("status",     status);
        props.put("projetId",   projetId);
        props.put("activiteId", activiteId);

        return new CalendarEventDto(
                type + "-" + id,
                buildTitle(type, (String) row[1], status),
                toLocalDate(row[3]) != null ? toLocalDate(row[3]).toString() : null,
                endExclusive,
                color,
                "#FFFFFF",
                true,
                props
        );
    }

    private String buildTitle(String type, String designation, String status) {
        String icon = switch (type) {
            case "PROJET"   -> "📁";
            case "ACTIVITE" -> "📋";
            case "TACHE"    -> "✅";
            default         -> "•";
        };
        String badge = switch (status) {
            case "TERMINE"    -> " ✔";
            case "ANNULE"     -> " ✖";
            case "EN_ATTENTE" -> " ⏸";
            default           -> "";
        };
        return icon + " " + designation + badge;
    }

    private LocalDate toLocalDate(Object value) {
        switch (value) {
            case null -> {
                return null;
            }

            // java.sql.Date (cas standard MySQL DATE)
            case java.sql.Date d -> {
                return d.toLocalDate();
            }

            // java.sql.Timestamp (cas MySQL DATETIME / TIMESTAMP)
            case java.sql.Timestamp ts -> {
                return ts.toLocalDateTime().toLocalDate();
            }

            // LocalDate déjà parsé par le driver
            case LocalDate ld -> {
                return ld;
            }

            // LocalDateTime
            case java.time.LocalDateTime ldt -> {
                return ldt.toLocalDate();
            }
            default -> {
            }
        }

        // Fallback String — gère "2026-03-15" ET "2026-03-15 01:00:00.0"
        String str = value.toString().trim();
        if (str.length() > 10)
            str = str.substring(0, 10); // on garde uniquement "yyyy-MM-dd"

        return LocalDate.parse(str);
    }

}
