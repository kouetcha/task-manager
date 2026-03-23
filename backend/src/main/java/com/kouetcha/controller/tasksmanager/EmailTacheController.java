package com.kouetcha.controller.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.service.tasksmanager.EmailTacheServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Emails Tache", description = "API de gestion des emails liés aux tâches")
@RequestMapping("/taches/{tacheId}/emails")
@RequiredArgsConstructor
public class EmailTacheController {

    private final EmailTacheServiceImpl emailTacheService;

    @Operation(
            summary = "Ajouter un email à une tâche",
            description = "Permet d'ajouter une adresse email à une tâche spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PostMapping
    public ResponseEntity<EmailTache> addEmail(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long tacheId,
            @Parameter(description = "Adresse email à ajouter", example = "contact@example.com")
            @RequestParam String email) {

        EmailTache created = emailTacheService.addEmail(tacheId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Supprimer un email",
            description = "Supprime un email via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> removeEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        emailTacheService.removeEmail(emailId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Activer un email",
            description = "Active un email existant pour qu'il puisse recevoir des notifications"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email activé avec succès"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @PutMapping("/{emailId}/activate")
    public ResponseEntity<EmailTache> activateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        EmailTache updated = emailTacheService.activateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Désactiver un email",
            description = "Désactive un email pour qu'il ne reçoive plus de notifications"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email désactivé avec succès"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @PutMapping("/{emailId}/deactivate")
    public ResponseEntity<EmailTache> deactivateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        EmailTache updated = emailTacheService.deactivateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Lister les emails d'une tâche",
            description = "Retourne la liste des emails associés à une tâche via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des emails récupérée"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @GetMapping
    public ResponseEntity<List<EmailTache>> findByTache(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long tacheId) {

        List<EmailTache> emails = emailTacheService.findByParent(tacheId);
        return ResponseEntity.ok(emails);
    }

    @Operation(
            summary = "Modifier une adresse email",
            description = "Met à jour l'adresse email d'un enregistrement existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @PatchMapping("/{emailId}")
    public ResponseEntity<EmailTache> updateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId,
            @Parameter(description = "Nouvelle adresse email", example = "newemail@example.com")
            @RequestParam String email) {

        EmailTache updated = emailTacheService.updateEmail(emailId, email);
        return ResponseEntity.ok(updated);
    }
}