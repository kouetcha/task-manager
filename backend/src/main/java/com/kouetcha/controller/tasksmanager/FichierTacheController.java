package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.ApiResponseSimple;
import com.kouetcha.model.tasksmanager.FichierEntityGestion;
import com.kouetcha.service.tasksmanager.FichierTacheServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@RequestMapping("fichiers-tache/{tacheId}")
@Tag(name = "Fichiers Tache", description = "API de gestion des fichiers liés aux tâches")
@RequiredArgsConstructor
public class FichierTacheController {

    private final FichierTacheServiceImpl fichierService;

    @Operation(
            summary = "Uploader un ou plusieurs fichiers",
            description = "Permet d'uploader un ou plusieurs fichiers pour une tâche spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichiers uploadés avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long tacheId,
            @Parameter(description = "Liste des fichiers à uploader")
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Noms personnalisés pour les fichiers (optionnel)")
            @RequestParam(required = false) List<String> nomsFichiers
    ) {
        for (int i = 0; i < files.size(); i++) {
            String nom = (nomsFichiers != null && nomsFichiers.size() > i) ? nomsFichiers.get(i) : null;
            fichierService.upload(tacheId, files.get(i), nom);
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
            summary = "Supprimer tous les fichiers d'une tâche",
            description = "Supprime tous les fichiers associés à une tâche"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tous les fichiers supprimés avec succès"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @DeleteMapping
    public ResponseEntity<?> deleteAllFiles(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long tacheId) {
        fichierService.deleteAllByParentId(tacheId);
        return ResponseEntity.ok(new ApiResponseSimple("Tous les fichiers du tache ont été supprimés"));
    }

    @Operation(
            summary = "Lister tous les fichiers d'une tâche",
            description = "Retourne la liste de tous les fichiers associés à une tâche"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des fichiers récupérée"),
            @ApiResponse(responseCode = "404", description = "Tâche non trouvée")
    })
    @GetMapping
    public ResponseEntity<List<FichierEntityGestion>> listFiles(
            @Parameter(description = "ID de la tâche", example = "1")
            @PathVariable Long tacheId) {
        List<FichierEntityGestion> fichiers = fichierService.findByParent(tacheId);
        return ResponseEntity.ok(fichiers);
    }
}