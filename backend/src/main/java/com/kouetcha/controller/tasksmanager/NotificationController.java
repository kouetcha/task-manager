package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.NotificationDto;
import com.kouetcha.model.tasksmanager.Notification;
import com.kouetcha.service.tasksmanager.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API de gestion des notifications utilisateur")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Créer une notification",
            description = "Permet de créer une nouvelle notification pour un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide")
    })
    @PostMapping
    public ResponseEntity<Notification> create(
            @Valid @RequestBody NotificationDto dto) {
        return ResponseEntity.ok(notificationService.create(dto));
    }

    @Operation(summary = "Récupérer toutes les notifications",
            description = "Retourne la liste complète des notifications d’un utilisateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getNotifications(
            @Parameter(description = "ID de l'utilisateur", example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotification(userId));
    }

    @Operation(summary = "Récupérer les notifications paginées",
            description = "Retourne les notifications d’un utilisateur avec pagination")
    @GetMapping("/{userId}/page")
    public ResponseEntity<Page<Notification>> getNotificationsPaginated(
            @Parameter(description = "ID de l'utilisateur", example = "1")
            @PathVariable Long userId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(notificationService.findNotification(userId, pageable));
    }

    @Operation(summary = "Récupérer les notifications non lues",
            description = "Retourne toutes les notifications non vues d’un utilisateur et les marque comme lues")
    @GetMapping("/{userId}/unseen")
    public ResponseEntity<List<Notification>> getUnseenNotifications(
            @Parameter(description = "ID de l'utilisateur", example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.findNotificationNoSeen(userId));
    }

    @Operation(summary = "Récupérer les notifications non lues (paginées)",
            description = "Retourne les notifications non vues avec pagination et les marque comme lues")
    @GetMapping("/{userId}/unseen/page")
    public ResponseEntity<Page<Notification>> getUnseenNotificationsPaginated(
            @Parameter(description = "ID de l'utilisateur", example = "1")
            @PathVariable Long userId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(notificationService.findNotificationNoSeen(userId, pageable));
    }

    @Operation(summary = "Compter les notifications non lues",
            description = "Retourne le nombre total de notifications non vues pour un utilisateur")
    @GetMapping("/{userId}/unseen/count")
    public ResponseEntity<Long> countUnseenNotifications(
            @Parameter(description = "ID de l'utilisateur", example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countNotificationNoSeen(userId));
    }

    @Operation(summary = "Marquer des notifications comme lues",
            description = "Marque une liste de notifications comme lues pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notifications marquées comme lues"),
            @ApiResponse(responseCode = "400", description = "Requête invalide")
    })
    @PatchMapping("/seen")
    public ResponseEntity<Void> markAsSeen(@RequestBody List<Long> ids) {
        notificationService.markAsSeen(ids);
        return ResponseEntity.noContent().build();
    }
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = " Les notifications ont été supprimés avec succès"),
            @ApiResponse(responseCode = "404", description = "Notification non trouvée")
    })
    @PatchMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody List<Long> ids) {
        notificationService.deletes(ids);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Marquer toutes les notifications comme lues",
            description = "Marque toutes les notifications de l'utilisateur connecté comme lues")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Toutes les notifications marquées comme lues")
    })
    @PatchMapping("/seen/all")
    public ResponseEntity<Void> markAllAsSeen() {
        notificationService.markAllAsSeen();
        return ResponseEntity.noContent().build();
    }
}