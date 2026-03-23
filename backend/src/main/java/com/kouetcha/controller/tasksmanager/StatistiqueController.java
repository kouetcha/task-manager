package com.kouetcha.controller.tasksmanager;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.dto.tasksmanager.CountMenuDto;
import com.kouetcha.dto.tasksmanager.DashboardDto;
import com.kouetcha.security.service.UserDetailsImpl;
import com.kouetcha.service.tasksmanager.StatistiqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Statistiques", description = "API de gestion des statistiques et tableaux de bord")
@RequiredArgsConstructor
@RequestMapping("statistiques")
public class StatistiqueController {

    private final StatistiqueService service;

    @Operation(
            summary = "Récupérer les données du tableau de bord",
            description = "Retourne les statistiques et indicateurs pour le tableau de bord de l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Données du tableau de bord récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(
            @Parameter(description = "Détails de l'utilisateur authentifié", hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) throws IllegalAccessException {
        if (UserContext.getUtilisaeurConnecte() == null) {
            throw new IllegalAccessException("Vous n'avez pas le droit d'acceder à la ressource");
        }
        Long userId = UserContext.getUtilisaeurConnecte().getId();
        String userEmail = UserContext.getUtilisaeurConnecte().getEmail();
        return ResponseEntity.ok(service.getDashboard(userId, userEmail));
    }

    @Operation(
            summary = "Récupérer les compteurs du menu",
            description = "Retourne les nombres d'éléments (projets, activités, tâches, etc.) pour l'affichage dans le menu de l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compteurs du menu récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @GetMapping("/menu")
    public ResponseEntity<CountMenuDto> getMenu(
            @Parameter(description = "Détails de l'utilisateur authentifié", hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) throws IllegalAccessException {
        if (UserContext.getUtilisaeurConnecte() == null) {
            throw new IllegalAccessException("Vous n'avez pas le droit d'acceder à la ressource");
        }
        String userEmail = UserContext.getUtilisaeurConnecte().getEmail();
        return ResponseEntity.ok(service.recupereMenuCount(userEmail));
    }
}