package com.kouetcha.controller.tasksmanager;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.tasksmanager.CalendarEventDto;
import com.kouetcha.service.tasksmanager.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Calendar", description = "API de gestion du calendrier utilisateur")
@RequiredArgsConstructor
@RequestMapping("calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(
            summary = "Récupérer les événements du calendrier",
            description = "Retourne les événements du calendrier pour un utilisateur connecté sur une période donnée"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des événements récupérée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé (utilisateur non authentifié)"),
            @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    })
    @GetMapping
    public ResponseEntity<List<CalendarEventDto>> getEvents(

            @Parameter(
                    description = "Date de début (format ISO yyyy-MM-dd)",
                    example = "2025-01-01"
            )
            @RequestParam
            @DateTimeFormat(iso = DATE)
            LocalDate start,

            @Parameter(
                    description = "Date de fin (format ISO yyyy-MM-dd)",
                    example = "2025-01-31"
            )
            @RequestParam
            @DateTimeFormat(iso = DATE)
            LocalDate end,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails

    ) throws IllegalAccessException {

        if (UserContext.getUtilisaeurConnecte() == null) {
            throw new IllegalAccessException("Vous n'avez pas le droit d'acceder à la ressource");
        }

        Long userId = UserContext.getUtilisaeurConnecte().getId();
        String userEmail = UserContext.getUtilisaeurConnecte().getEmail();

        return ResponseEntity.ok(
                calendarService.getCalendarEvents(userId, userEmail, start, end)
        );
    }
}