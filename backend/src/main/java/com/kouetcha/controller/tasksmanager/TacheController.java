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
@RestController
@CrossOrigin(origins = "${client.url}")
@Tag(name = "Taches")
@RequiredArgsConstructor
@RequestMapping("taches")
public class TacheController {

    private final TacheService tacheService;


    @PostMapping(path = "/activites/{activiteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Tache> create(@PathVariable Long activiteId,
                                        @ModelAttribute BaseEntityGestionDto dto) {
        Tache tache = tacheService.create(dto, activiteId);
        return ResponseEntity.ok(tache);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tache> update(@PathVariable Long id,
                                        @RequestBody BaseEntityGestionDto dto) {
        Tache tache = tacheService.update(id, dto);
        return ResponseEntity.ok(tache);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        tacheService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Tâche supprimée"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tache> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.findById(id));
    }

    @GetMapping("/activites/{activiteId}")
    public ResponseEntity<List<Tache>> findByActivite(@PathVariable Long activiteId) {
        return ResponseEntity.ok(tacheService.findByActiviteId(activiteId));
    }
    @GetMapping("/activites/{activiteId}/email/{email}")
    public ResponseEntity<List<Tache>> findByActiviteAndEmail(@PathVariable Long activiteId,@PathVariable String email) {
        return ResponseEntity.ok(tacheService.findByActiviteIdAndEmail(activiteId,email));
    }

    @PostMapping("/{id}/fichiers")
    public ResponseEntity<?> addFichier(@PathVariable Long id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "nom", required = false) String nom) {
        tacheService.addFichier(id, new FichierDTO(file, nom));
        return ResponseEntity.ok(new ApiResponse("Fichier ajouté"));
    }

    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<?> deleteFichier(@PathVariable Long id, @PathVariable Long fichierId) {
        tacheService.deleteFichier(id, fichierId);
        return ResponseEntity.ok(new ApiResponse("Fichier supprimé"));
    }

    @PatchMapping("/{id}/designation")
    public ResponseEntity<Tache> updateDesignation(@PathVariable Long id, @RequestBody TexteDto dto) {
        Tache tache = tacheService.updateDesignation(id, dto);
        return ResponseEntity.ok(tache);
    }
    @PatchMapping("/{id}/description")
    public ResponseEntity<Tache> updateDescription(@PathVariable Long id, @RequestBody TexteDto dto) {
        Tache tache = tacheService.updateDescription(id, dto);
        return ResponseEntity.ok(tache);
    }
    @PatchMapping("/{id}/date-debut")
    public ResponseEntity<Tache> updateDateDebut(@PathVariable Long id, @RequestBody DateDto dto) {
        Tache tache = tacheService.updateDateDebut(id, dto);
        return ResponseEntity.ok(tache);
    }
    @PatchMapping("/{id}/date-fin")
    public ResponseEntity<Tache> updateDateFin(@PathVariable Long id, @RequestBody DateDto dto) {
        Tache tache = tacheService.updateDateFin(id, dto);
        return ResponseEntity.ok(tache);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Page<Tache>> findByEmail(@PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(tacheService.findByEmail(email,pageable));
    }
    @GetMapping("/email/{email}/dto-page")
    public ResponseEntity<Page<BaseEntityDto>> findDtoActiveByEmailPage(@PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(tacheService.findDtoActiveByEmail(email,pageable));
    }
    @GetMapping("/email/{email}/dto-list")
    public ResponseEntity<List<BaseEntityDto>> findDtoActiveByEmailList(@PathVariable String email) {
        return ResponseEntity.ok(tacheService.findDtoActiveByEmail(email));
    }

}
