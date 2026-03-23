package com.kouetcha.controller.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailProjet;
import com.kouetcha.service.tasksmanager.EmailProjetServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Emails Projet", description = "API de gestion des emails liés aux projets")
@RequestMapping("/projets/{projetId}/emails")
@RequiredArgsConstructor
public class EmailProjetController {

    private final EmailProjetServiceImpl emailProjetService;

    @Operation(
            summary = "Ajouter un email à un projet",
            description = "Permet d'ajouter une adresse email à un projet spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PostMapping
    public ResponseEntity<EmailProjet> addEmail(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long projetId,
            @Parameter(description = "Adresse email à ajouter", example = "contact@example.com")
            @RequestParam String email) {

        EmailProjet created = emailProjetService.addEmail(projetId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
    public ResponseEntity<EmailProjet> updateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId,
            @Parameter(description = "Nouvelle adresse email", example = "newemail@example.com")
            @RequestParam String email) {

        EmailProjet updated = emailProjetService.updateEmail(emailId, email);
        return ResponseEntity.ok(updated);
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

        emailProjetService.removeEmail(emailId);
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
    public ResponseEntity<EmailProjet> activateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        EmailProjet updated = emailProjetService.activateEmail(emailId);
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
    public ResponseEntity<EmailProjet> deactivateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        EmailProjet updated = emailProjetService.deactivateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Lister les emails d'un projet",
            description = "Retourne la liste des emails associés à un projet via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des emails récupérée"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @GetMapping
    public ResponseEntity<List<EmailProjet>> findByProjet(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long projetId) {

        List<EmailProjet> emails = emailProjetService.findByParent(projetId);
        return ResponseEntity.ok(emails);
    }
}