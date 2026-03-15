package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.ApiResponse;
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

@RestController
@CrossOrigin(origins = "${client.url}")
@RequestMapping("fichiers-tache/{tacheId}")
@Tag(name = "Fichiers Tache")
@RequiredArgsConstructor
public class FichierTacheController {

    private final FichierTacheServiceImpl fichierService;



    /**
     * Upload d'un ou plusieurs fichiers pour un tache
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(
            @PathVariable Long tacheId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) List<String> nomsFichiers
    ) {
        for (int i = 0; i < files.size(); i++) {
            String nom = (nomsFichiers != null && nomsFichiers.size() > i) ? nomsFichiers.get(i) : null;
            fichierService.upload(tacheId, files.get(i), nom);
        }
        return ResponseEntity.ok(new ApiResponse("Fichiers uploadés avec succès"));
    }

    /**
     * Télécharger un fichier par son chemin ou code
     */
    @GetMapping("/download/{fileCode}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileCode) {
        return fichierService.downloadFile(fileCode);

    }
    @PostMapping("/onlyoffice-save/{id}")
    public ResponseEntity<Map<String, Integer>> saveDocumentOnLyOffice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(fichierService.saveDocumentOnLyOffice(id,payload));
    }

    /**
     * Supprimer un fichier par son id
     */
    @DeleteMapping("/{fichierId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fichierId) {
        fichierService.deleteById(fichierId);
        return ResponseEntity.ok(new ApiResponse("Fichier supprimé avec succès"));
    }

    /**
     * Supprimer tous les fichiers d'un tache
     */
    @DeleteMapping
    public ResponseEntity<?> deleteAllFiles(@PathVariable Long tacheId) {
        fichierService.deleteAllByParentId(tacheId);
        return ResponseEntity.ok(new ApiResponse("Tous les fichiers du tache ont été supprimés"));
    }

    /**
     * Lister tous les fichiers d'un tache
     */
    @GetMapping
    public ResponseEntity<List<FichierEntityGestion>> listFiles(@PathVariable Long tacheId) {
        List<FichierEntityGestion> fichiers = fichierService.findByParent(tacheId);
        return ResponseEntity.ok(fichiers);
    }
}