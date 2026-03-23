package com.kouetcha.controller.tasksmanager;

import com.fasterxml.jackson.annotation.JsonView;
import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.CommentaireActivite;
import com.kouetcha.service.tasksmanager.CommentaireActiviteServiceImpl;
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
@Tag(name = "Commentaire Activite", description = "API de gestion des commentaires liés aux activités")
@RequiredArgsConstructor
@RequestMapping("commentaires-activite")
public class CommentaireActiviteController {

    private final CommentaireActiviteServiceImpl commentaireActiviteService;

    @Operation(
            summary = "Créer un commentaire",
            description = "Créer un nouveau commentaire pour une activité avec éventuellement un fichier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentaireActivite> create(

            @Parameter(description = "Données du commentaire (multipart/form-data)")
            @Valid @ModelAttribute CommentaireDto dto) {

        CommentaireActivite commentaireActivite = commentaireActiviteService.create(dto);
        return ResponseEntity.ok(commentaireActivite);
    }

    @Operation(
            summary = "Modifier le contenu d’un commentaire",
            description = "Met à jour uniquement le texte d’un commentaire existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire modifié avec succès"),
            @ApiResponse(responseCode = "404", description = "Commentaire non trouvé")
    })
    @PatchMapping
    public ResponseEntity<CommentaireActivite> changeContenu(

            @Parameter(description = "Nouveau contenu du commentaire")
            @RequestBody CommentaireUpdateDto dto) {

        CommentaireActivite commentaireActivite = commentaireActiviteService.changeContenu(dto);
        return ResponseEntity.ok(commentaireActivite);
    }

    @Operation(
            summary = "Supprimer un commentaire",
            description = "Supprime un commentaire par son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commentaire supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Commentaire non trouvé")
    })
    @DeleteMapping("/{comId}")
    public ResponseEntity<ApiResponseSimple> delete(

            @Parameter(description = "ID du commentaire", example = "1")
            @PathVariable Long comId) {

        commentaireActiviteService.delete(comId);
        return ResponseEntity.ok(new ApiResponseSimple("Commentaire supprimé avec succès"));
    }

    @Operation(
            summary = "Lister les commentaires d’une activité",
            description = "Retourne les commentaires liés à une activité (via parentId)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des commentaires récupérée"),
            @ApiResponse(responseCode = "404", description = "Activité non trouvée")
    })
    @GetMapping("{parentId}")
    public ResponseEntity<List<CommentaireResponseDto>> findByProjet(

            @Parameter(description = "ID de l'activité (parentId)", example = "1")
            @PathVariable Long parentId) {

        return ResponseEntity.ok(commentaireActiviteService.findByParentDto(parentId));
    }
}