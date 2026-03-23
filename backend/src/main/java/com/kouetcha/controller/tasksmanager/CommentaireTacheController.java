package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.ApiResponseSimple;
import com.kouetcha.dto.tasksmanager.CommentaireDto;
import com.kouetcha.dto.tasksmanager.CommentaireResponseDto;
import com.kouetcha.dto.tasksmanager.CommentaireUpdateDto;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.model.tasksmanager.CommentaireTache;
import com.kouetcha.service.tasksmanager.CommentaireTacheServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@Tag(name = "Commentaire Tache", description = "API de gestion des commentaires liés aux tâches")
@RequiredArgsConstructor
@RequestMapping("commentaires-tache")
public class CommentaireTacheController {

    private final CommentaireTacheServiceImpl commentaireTacheService;

    @Operation(
            summary = "Créer un commentaire de tâche",
            description = "Permet de créer un commentaire pour une tâche avec possibilité d’ajouter un fichier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentaireTache> create(

            @Parameter(description = "Données du commentaire (multipart/form-data)")
            @Valid @ModelAttribute CommentaireDto dto) {

        CommentaireTache commentaireTache = commentaireTacheService.create(dto);
        return ResponseEntity.ok(commentaireTache);
    }

    @Operation(
            summary = "Modifier le contenu d’un commentaire de tâche",
            description = "Met à jour uniquement le texte d’un commentaire existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire modifié avec succès"),
            @ApiResponse(responseCode = "404", description = "Commentaire non trouvé")
    })
    @PatchMapping
    public ResponseEntity<CommentaireTache> changeContenu(

            @Parameter(description = "Nouveau contenu du commentaire")
            @RequestBody CommentaireUpdateDto dto) {

        CommentaireTache commentaireTache = commentaireTacheService.changeContenu(dto);
        return ResponseEntity.ok(commentaireTache);
    }

    @Operation(
            summary = "Supprimer un commentaire de tâche",
            description = "Supprime un commentaire via son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Commentaire non trouvé")
    })
    @DeleteMapping("/{comId}")
    public ResponseEntity<ApiResponseSimple> delete(

            @Parameter(description = "ID du commentaire", example = "1")
            @PathVariable Long comId) {

        commentaireTacheService.delete(comId);
        return ResponseEntity.ok(new ApiResponseSimple("Commentaire supprimé avec succès"));
    }

    @Operation(
            summary = "Lister les commentaires d’une tâche",
            description = "Retourne la liste des commentaires associés à une tâche via son ID (parentId)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des commentaires récupérée"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @GetMapping("{parentId}")
    public ResponseEntity<List<CommentaireResponseDto>> findByProjet(

            @Parameter(description = "ID de la tâche (parentId)", example = "1")
            @PathVariable Long parentId) {

        return ResponseEntity.ok(commentaireTacheService.findByParentDto(parentId));
    }
}