package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Projet;
import com.kouetcha.service.tasksmanager.ProjetService;
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
@RequestMapping("projets")
@Tag(name = "Projets", description = "API de gestion des projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    @Operation(
            summary = "Créer un projet",
            description = "Permet de créer un nouveau projet avec possibilité d'ajouter un fichier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projet créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Projet> create(
            @Parameter(description = "Données du projet (multipart/form-data)")
            @ModelAttribute BaseEntityGestionDto dto) {
        Projet projet = projetService.create(dto);
        return ResponseEntity.ok(projet);
    }

    @Operation(
            summary = "Modifier un projet",
            description = "Met à jour les informations d'un projet existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projet modifié avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Projet> update(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Données du projet à mettre à jour")
            @RequestBody BaseEntityGestionDto dto) {
        Projet projet = projetService.update(id, dto);
        return ResponseEntity.ok(projet);
    }

    @Operation(
            summary = "Modifier la désignation d'un projet",
            description = "Met à jour uniquement la désignation d'un projet existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Désignation modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PatchMapping("/{id}/designation")
    public ResponseEntity<Projet> updateDesignation(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle désignation")
            @RequestBody TexteDto dto) {
        Projet projet = projetService.updateDesignation(id, dto);
        return ResponseEntity.ok(projet);
    }

    @Operation(
            summary = "Modifier la description d'un projet",
            description = "Met à jour uniquement la description d'un projet existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Description modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PatchMapping("/{id}/description")
    public ResponseEntity<Projet> updateDescription(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle description")
            @RequestBody TexteDto dto) {
        Projet projet = projetService.updateDescription(id, dto);
        return ResponseEntity.ok(projet);
    }

    @Operation(
            summary = "Modifier la date de début d'un projet",
            description = "Met à jour uniquement la date de début d'un projet existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Date de début modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PatchMapping("/{id}/date-debut")
    public ResponseEntity<Projet> updateDateDebut(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle date de début")
            @RequestBody DateDto dto) {
        Projet projet = projetService.updateDateDebut(id, dto);
        return ResponseEntity.ok(projet);
    }

    @Operation(
            summary = "Modifier la date de fin d'un projet",
            description = "Met à jour uniquement la date de fin d'un projet existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Date de fin modifiée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PatchMapping("/{id}/date-fin")
    public ResponseEntity<Projet> updateDateFin(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle date de fin")
            @RequestBody DateDto dto) {
        Projet projet = projetService.updateDateFin(id, dto);
        return ResponseEntity.ok(projet);
    }

    @Operation(
            summary = "Supprimer un projet",
            description = "Supprime un projet via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projet supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id) {
        projetService.delete(id);
        return ResponseEntity.ok(new ApiResponseSimple("Projet supprimé"));
    }

    @Operation(
            summary = "Trouver un projet par son ID",
            description = "Retourne les informations d'un projet spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projet trouvé"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Projet> findById(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(projetService.findById(id));
    }

    @Operation(
            summary = "Lister tous les projets",
            description = "Retourne la liste de tous les projets"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des projets récupérée")
    })
    @GetMapping
    public ResponseEntity<List<Projet>> findAll() {
        return ResponseEntity.ok(projetService.findAll());
    }

    @Operation(
            summary = "Ajouter un fichier à un projet",
            description = "Permet d'ajouter un fichier à un projet spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier ajouté avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Projet non trouvé")
    })
    @PostMapping("/{id}/fichiers")
    public ResponseEntity<?> addFichier(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Fichier à ajouter")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nom du fichier (optionnel)", example = "document.pdf")
            @RequestParam(value = "nom", required = false) String nom) {
        projetService.addFichier(id, new FichierDTO(file, nom));
        return ResponseEntity.ok(new ApiResponseSimple("Fichier ajouté"));
    }

    @Operation(
            summary = "Supprimer un fichier d'un projet",
            description = "Supprime un fichier associé à un projet spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Projet ou fichier non trouvé")
    })
    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<?> deleteFichier(
            @Parameter(description = "ID du projet", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID du fichier", example = "1")
            @PathVariable Long fichierId) {
        projetService.deleteFichier(id, fichierId);
        return ResponseEntity.ok(new ApiResponseSimple("Fichier supprimé"));
    }

    @Operation(
            summary = "Trouver les projets par email",
            description = "Retourne une page de projets associés à un email avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des projets récupérée"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<Page<Projet>> findByEmail(
            @Parameter(description = "Adresse email", example = "user@example.com")
            @PathVariable String email,
            Pageable pageable) {
        return ResponseEntity.ok(projetService.findByEmail(email, pageable));
    }

    @Operation(
            summary = "Trouver les DTOs de projets actifs par email (page)",
            description = "Retourne une page de DTOs de projets actifs associés à un email avec pagination"
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
        return ResponseEntity.ok(projetService.findDtoActiveByEmail(email, pageable));
    }

    @Operation(
            summary = "Trouver les DTOs de projets actifs par email (liste)",
            description = "Retourne une liste de DTOs de projets actifs associés à un email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des DTOs récupérée"),
            @ApiResponse(responseCode = "404", description = "Email non trouvé")
    })
    @GetMapping("/email/{email}/dto-list")
    public ResponseEntity<List<BaseEntityDto>> findDtoActiveByEmailList(
            @Parameter(description = "Adresse email", example = "user@example.com")
            @PathVariable String email) {
        return ResponseEntity.ok(projetService.findDtoActiveByEmail(email));
    }
}