package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Tache;
import com.kouetcha.service.tasksmanager.TacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Taches", description = "API de gestion des tâches")
@RequiredArgsConstructor
@RequestMapping("taches")
public class TacheController {

    private final TacheService tacheService;

    @Operation(
            summary = "Créer une tâche",
            description = "Permet de créer une nouvelle tâche pour une activité spécifique avec possibilité d'ajouter un fichier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Activité non trouvée")
    })
    @PostMapping(path = "/activites/{activiteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Tache> create(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long activiteId,
            @Parameter(description = "Données de la tâche (multipart/form-data)")
            @ModelAttribute BaseEntityGestionDto dto) {
        Tache tache = tacheService.create(dto, activiteId);
        return ResponseEntity.ok(tache);
    }

    @Operation(
            summary = "Modifier une tâche",
            description = "Met à jour les informations d'une tâche existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Tache> update(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Données de la tâche à mettre à jour")
            @RequestBody BaseEntityGestionDto dto) {
        Tache tache = tacheService.update(id, dto);
        return ResponseEntity.ok(tache);
    }

    @Operation(
            summary = "Supprimer une tâche",
            description = "Supprime une tâche via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id) {
        tacheService.delete(id);
        return ResponseEntity.ok(new ApiResponseSimple("Tâche supprimée"));
    }

    @Operation(
            summary = "Trouver une tâche par son ID",
            description = "Retourne les informations d'une tâche spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tâche trouvée"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Tache> findById(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(tacheService.findById(id));
    }

    @Operation(
            summary = "Lister les tâches d'une activité",
            description = "Retourne la liste de toutes les tâches associées à une activité"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tâches récupérée"),
            @ApiResponse(responseCode = "404", description = "Activité non trouvée")
    })
    @GetMapping("/activites/{activiteId}")
    public ResponseEntity<List<Tache>> findByActivite(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long activiteId) {
        return ResponseEntity.ok(tacheService.findByActiviteId(activiteId));
    }

    @Operation(
            summary = "Lister les tâches d'une activité par email",
            description = "Retourne la liste des tâches associées à une activité et à un email spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tâches récupérée"),
            @ApiResponse(responseCode = "404", description = "Activité ou email non trouvé")
    })
    @GetMapping("/activites/{activiteId}/email/{email}")
    public ResponseEntity<List<Tache>> findByActiviteAndEmail(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long activiteId,
            @Parameter(description = "Adresse email", example = "user@example.com")
            @PathVariable String email) {
        return ResponseEntity.ok(tacheService.findByActiviteIdAndEmail(activiteId, email));
    }

    @Operation(
            summary = "Ajouter un fichier à une tâche",
            description = "Permet d'ajouter un fichier à une tâche spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PostMapping("/{id}/fichiers")
    public ResponseEntity<?> addFichier(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Fichier à ajouter")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nom du fichier (optionnel)", example = "document.pdf")
            @RequestParam(value = "nom", required = false) String nom) {
        tacheService.addFichier(id, new FichierDTO(file, nom));
        return ResponseEntity.ok(new ApiResponseSimple("Fichier ajouté"));
    }

    @Operation(
            summary = "Supprimer un fichier d'une tâche",
            description = "Supprime un fichier associé à une tâche spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Tâche ou fichier non trouvé")
    })
    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<?> deleteFichier(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID du fichier", example = "1")
            @PathVariable Long fichierId) {
        tacheService.deleteFichier(id, fichierId);
        return ResponseEntity.ok(new ApiResponseSimple("Fichier supprimé"));
    }

    @Operation(
            summary = "Modifier la désignation d'une tâche",
            description = "Met à jour uniquement la désignation d'une tâche existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Désignation modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PatchMapping("/{id}/designation")
    public ResponseEntity<Tache> updateDesignation(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle désignation")
            @RequestBody TexteDto dto) {
        Tache tache = tacheService.updateDesignation(id, dto);
        return ResponseEntity.ok(tache);
    }

    @Operation(
            summary = "Modifier la description d'une tâche",
            description = "Met à jour uniquement la description d'une tâche existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Description modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PatchMapping("/{id}/description")
    public ResponseEntity<Tache> updateDescription(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle description")
            @RequestBody TexteDto dto) {
        Tache tache = tacheService.updateDescription(id, dto);
        return ResponseEntity.ok(tache);
    }

    @Operation(
            summary = "Modifier la date de début d'une tâche",
            description = "Met à jour uniquement la date de début d'une tâche existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Date de début modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PatchMapping("/{id}/date-debut")
    public ResponseEntity<Tache> updateDateDebut(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle date de début")
            @RequestBody DateDto dto) {
        Tache tache = tacheService.updateDateDebut(id, dto);
        return ResponseEntity.ok(tache);
    }

    @Operation(
            summary = "Modifier la date de fin d'une tâche",
            description = "Met à jour uniquement la date de fin d'une tâche existante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Date de fin modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PatchMapping("/{id}/date-fin")
    public ResponseEntity<Tache> updateDateFin(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle date de fin")
            @RequestBody DateDto dto) {
        Tache tache = tacheService.updateDateFin(id, dto);
        return ResponseEntity.ok(tache);
    }

    @Operation(
            summary = "Trouver les tâches par email",
            description = "Retourne une page de tâches associées à un email avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des tâches récupérée"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<Page<Tache>> findByEmail(
            @Parameter(description = "Adresse email", example = "user@example.com")
            @PathVariable String email,
            Pageable pageable) {
        return ResponseEntity.ok(tacheService.findByEmail(email, pageable));
    }

    @Operation(
            summary = "Trouver les DTOs de tâches actives par email (page)",
            description = "Retourne une page de DTOs de tâches actives associées à un email avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des DTOs récupérée"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @GetMapping("/email/{email}/dto-page")
    public ResponseEntity<Page<BaseEntityDto>> findDtoActiveByEmailPage(
            @Parameter(description = "Adresse email", example = "user@example.com")
            @PathVariable String email,
            Pageable pageable) {
        return ResponseEntity.ok(tacheService.findDtoActiveByEmail(email, pageable));
    }

    @Operation(
            summary = "Trouver les DTOs de tâches actives par email (liste)",
            description = "Retourne une liste de DTOs de tâches actives associées à un email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des DTOs récupérée"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @GetMapping("/email/{email}/dto-list")
    public ResponseEntity<List<BaseEntityDto>> findDtoActiveByEmailList(
            @Parameter(description = "Adresse email", example = "user@example.com")
            @PathVariable String email) {
        return ResponseEntity.ok(tacheService.findDtoActiveByEmail(email));
    }
}