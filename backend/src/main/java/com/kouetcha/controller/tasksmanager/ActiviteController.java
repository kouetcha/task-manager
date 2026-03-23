package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Activite;

import com.kouetcha.service.tasksmanager.ActiviteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Activites", description = "API de gestion des activités")
@RequiredArgsConstructor
@RequestMapping("activites")
public class ActiviteController {

    private final ActiviteService activiteService;

    @Operation(summary = "Créer une activité", description = "Créer une activité liée à un projet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activité créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide")
    })
    @PostMapping(path = "/projet/{projetId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Activite> create(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long projetId,

            @Parameter(description = "Données de l'activité")
            @ModelAttribute BaseEntityGestionDto dto) {

        Activite activite = activiteService.create(dto, projetId);
        return ResponseEntity.ok(activite);
    }

    @Operation(summary = "Mettre à jour une activité")
    @PutMapping("/{id}")
    public ResponseEntity<Activite> update(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long id,

            @RequestBody BaseEntityGestionDto dto) {

        Activite activite = activiteService.update(id, dto);
        return ResponseEntity.ok(activite);
    }

    @Operation(summary = "Supprimer une activité")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long id) {

        activiteService.delete(id);
        return ResponseEntity.ok(new ApiResponseSimple("Activité supprimée"));
    }

    @Operation(summary = "Récupérer une activité par ID")
    @GetMapping("/{id}")
    public ResponseEntity<Activite> findById(
            @Parameter(description = "ID de l'activité", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(activiteService.findById(id));
    }

    @Operation(summary = "Lister les activités d’un projet")
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<Activite>> findByProjet(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long projetId) {

        return ResponseEntity.ok(activiteService.findByProjetId(projetId));
    }

    @Operation(summary = "Lister les activités d’un projet par email utilisateur")
    @GetMapping("/projet/{projetId}/email/{email}")
    public ResponseEntity<List<Activite>> findByProjetAndEMail(
            @PathVariable Long projetId,
            @PathVariable String email) {

        return ResponseEntity.ok(activiteService.findByProjetIdAndEmail(projetId, email));
    }

    @Operation(summary = "Lister les activités paginées par email")
    @GetMapping("/email/{email}")
    public ResponseEntity<Page<Activite>> findByEmail(
            @PathVariable String email,
            Pageable pageable) {

        return ResponseEntity.ok(activiteService.findByEmail(email, pageable));
    }

    @Operation(summary = "Lister les activités (DTO) paginées par email")
    @GetMapping("/email/{email}/dto-page")
    public ResponseEntity<Page<BaseEntityDto>> findDtoActiveByEmailPage(
            @PathVariable String email,
            Pageable pageable) {

        return ResponseEntity.ok(activiteService.findDtoActiveByEmail(email, pageable));
    }

    @Operation(summary = "Lister les activités (DTO) par email")
    @GetMapping("/email/{email}/dto-list")
    public ResponseEntity<List<BaseEntityDto>> findDtoActiveByEmailList(
            @PathVariable String email) {

        return ResponseEntity.ok(activiteService.findDtoActiveByEmail(email));
    }

    @Operation(summary = "Ajouter un fichier à une activité")
    @PostMapping("/{id}/fichiers")
    public ResponseEntity<?> addFichier(
            @Parameter(description = "ID de l'activité")
            @PathVariable Long id,

            @Parameter(description = "Fichier à uploader")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Nom du fichier")
            @RequestParam(value = "nom", required = false) String nom) {

        activiteService.addFichier(id, new FichierDTO(file, nom));
        return ResponseEntity.ok(new ApiResponseSimple("Fichier ajouté"));
    }

    @Operation(summary = "Supprimer un fichier d’une activité")
    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<?> deleteFichier(
            @PathVariable Long id,
            @PathVariable Long fichierId) {

        activiteService.deleteFichier(id, fichierId);
        return ResponseEntity.ok(new ApiResponseSimple("Fichier supprimé"));
    }

    @Operation(summary = "Mettre à jour la désignation")
    @PatchMapping("/{id}/designation")
    public ResponseEntity<Activite> updateDesignation(
            @PathVariable Long id,
            @RequestBody TexteDto dto) {

        return ResponseEntity.ok(activiteService.updateDesignation(id, dto));
    }

    @Operation(summary = "Mettre à jour la description")
    @PatchMapping("/{id}/description")
    public ResponseEntity<Activite> updateDescription(
            @PathVariable Long id,
            @RequestBody TexteDto dto) {

        return ResponseEntity.ok(activiteService.updateDescription(id, dto));
    }

    @Operation(summary = "Mettre à jour la date de début")
    @PatchMapping("/{id}/date-debut")
    public ResponseEntity<Activite> updateDateDebut(
            @PathVariable Long id,
            @RequestBody DateDto dto) {

        return ResponseEntity.ok(activiteService.updateDateDebut(id, dto));
    }

    @Operation(summary = "Mettre à jour la date de fin")
    @PatchMapping("/{id}/date-fin")
    public ResponseEntity<Activite> updateDateFin(
            @PathVariable Long id,
            @RequestBody DateDto dto) {

        return ResponseEntity.ok(activiteService.updateDateFin(id, dto));
    }
}