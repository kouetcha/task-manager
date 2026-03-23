package com.kouetcha.controller.tasksmanager;

import com.kouetcha.model.tasksmanager.EmailActivite;
import com.kouetcha.model.tasksmanager.EmailTache;
import com.kouetcha.service.tasksmanager.EmailActiviteServiceImpl;
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
@Tag(name = "Emails Activite", description = "API de gestion des emails liés aux activités")
@RequestMapping("/activites/{activiteId}/emails")
@RequiredArgsConstructor
public class EmailActiviteController {

    private final EmailActiviteServiceImpl emailActiviteService;

    @Operation(
            summary = "Ajouter un email à une activité",
            description = "Permet d'ajouter une adresse email à une activité spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Activité non trouvée")
    })
    @PostMapping
    public ResponseEntity<EmailActivite> addEmail(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long activiteId,
            @Parameter(description = "Adresse email à ajouter", example = "contact@example.com")
            @RequestParam String email) {

        EmailActivite created = emailActiviteService.addEmail(activiteId, email);
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

        emailActiviteService.removeEmail(emailId);
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
    public ResponseEntity<EmailActivite> activateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        EmailActivite updated = emailActiviteService.activateEmail(emailId);
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
    public ResponseEntity<EmailActivite> deactivateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId) {

        EmailActivite updated = emailActiviteService.deactivateEmail(emailId);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Lister les emails d'une activité",
            description = "Retourne la liste des emails associés à une activité via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des emails récupérée"),
            @ApiResponse(responseCode = "404", description = "Activité non trouvée")
    })
    @GetMapping
    public ResponseEntity<List<EmailActivite>> findByActivite(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long activiteId) {

        List<EmailActivite> emails = emailActiviteService.findByParent(activiteId);
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
    public ResponseEntity<EmailActivite> updateEmail(
            @Parameter(description = "ID de l'email", example = "1")
            @PathVariable Long emailId,
            @Parameter(description = "Nouvelle adresse email", example = "newemail@example.com")
            @RequestParam String email) {

        EmailActivite updated = emailActiviteService.updateEmail(emailId, email);
        return ResponseEntity.ok(updated);
    }
}