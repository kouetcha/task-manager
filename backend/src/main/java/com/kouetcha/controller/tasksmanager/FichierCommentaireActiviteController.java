package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.ApiResponseSimple;
import com.kouetcha.model.tasksmanager.FichierCommentaire;
import com.kouetcha.service.tasksmanager.FichierCommentaireActiviteServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
@RestController
@CrossOrigin(origins = "${client.url}")
@RequestMapping("fichiers-commentaire-activite/{commentaireId}")
@RequiredArgsConstructor
@Tag(name = "Fichiers Commentaire Activite", description = "API de gestion des fichiers liés aux commentaires d'activités")
public class FichierCommentaireActiviteController {

    private final FichierCommentaireActiviteServiceImpl fichierService;

    @Operation(
            summary = "Uploader un ou plusieurs fichiers",
            description = "Permet d'uploader un ou plusieurs fichiers pour un commentaire d'activité spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichiers uploadés avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Commentaire d'activité non trouvé")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @Parameter(description = "ID du commentaire d'activité", example = "1")
            @PathVariable Long commentaireId,
            @Parameter(description = "Liste des fichiers à uploader")
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Noms personnalisés pour les fichiers (optionnel)")
            @RequestParam(required = false) List<String> nomsFichiers
    ) {
        for (int i = 0; i < files.size(); i++) {
            String nom = (nomsFichiers != null && nomsFichiers.size() > i) ? nomsFichiers.get(i) : null;
            fichierService.upload(commentaireId, files.get(i), nom);
        }
        return ResponseEntity.ok(new ApiResponseSimple("Fichiers uploadés avec succès"));
    }

    @Operation(
            summary = "Télécharger un fichier",
            description = "Télécharge un fichier par son code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier téléchargé avec succès"),
            @ApiResponse(responseCode = "404", description = "Fichier non trouvé")
    })
    @GetMapping("/download/{fileCode}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Code du fichier", example = "abc123")
            @PathVariable String fileCode) {
        return fichierService.downloadFile(fileCode);
    }

    @Operation(
            summary = "Sauvegarder un document via OnlyOffice",
            description = "Sauvegarde un document édité via l'intégration OnlyOffice"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document sauvegardé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Fichier non trouvé")
    })
    @PostMapping("/onlyoffice-save/{id}")
    public ResponseEntity<Map<String, Integer>> saveDocumentOnLyOffice(
            @Parameter(description = "ID du fichier", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Payload de sauvegarde OnlyOffice")
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(fichierService.saveDocumentOnLyOffice(id, payload));
    }

    @Operation(
            summary = "Supprimer un fichier",
            description = "Supprime un fichier par son ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Fichier non trouvé")
    })
    @DeleteMapping("/{fichierId}")
    public ResponseEntity<?> deleteFile(
            @Parameter(description = "ID du fichier", example = "1")
            @PathVariable Long fichierId) {
        fichierService.deleteById(fichierId);
        return ResponseEntity.ok(new ApiResponseSimple("Fichier supprimé avec succès"));
    }

    @Operation(
            summary = "Supprimer tous les fichiers d'un commentaire d'activité",
            description = "Supprime tous les fichiers associés à un commentaire d'activité"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tous les fichiers supprimés avec succès"),
            @ApiResponse(responseCode = "404", description = "Commentaire d'activité non trouvé")
    })
    @DeleteMapping
    public ResponseEntity<?> deleteAllFiles(
            @Parameter(description = "ID du commentaire d'activité", example = "1")
            @PathVariable Long commentaireId) {
        fichierService.deleteAllByParentId(commentaireId);
        return ResponseEntity.ok(new ApiResponseSimple("Tous les fichiers du commentaireActivite ont été supprimés"));
    }

    @Operation(
            summary = "Lister tous les fichiers d'un commentaire d'activité",
            description = "Retourne la liste de tous les fichiers associés à un commentaire d'activité"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des fichiers récupérée"),
            @ApiResponse(responseCode = "404", description = "Commentaire d'activité non trouvé")
    })
    @GetMapping
    public ResponseEntity<List<FichierCommentaire>> listFiles(
            @Parameter(description = "ID du commentaire d'activité", example = "1")
            @PathVariable Long commentaireId) {
        List<FichierCommentaire> fichiers = fichierService.findByParent(commentaireId);
        return ResponseEntity.ok(fichiers);
    }
}