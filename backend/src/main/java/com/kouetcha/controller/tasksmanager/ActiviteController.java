package com.kouetcha.controller.tasksmanager;

import com.kouetcha.dto.tasksmanager.*;
import com.kouetcha.model.tasksmanager.Activite;

import com.kouetcha.service.tasksmanager.ActiviteService;

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
@Tag(name = "Activites")
@RequiredArgsConstructor
@RequestMapping("activites")
public class ActiviteController {

    private final ActiviteService activiteService;



    @PostMapping( path = "/projet/{projetId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Activite> create(@PathVariable Long projetId,
                                           @ModelAttribute BaseEntityGestionDto dto) {
        Activite activite = activiteService.create(dto, projetId);
        return ResponseEntity.ok(activite);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activite> update(@PathVariable Long id,
                                           @RequestBody BaseEntityGestionDto dto) {
        Activite activite = activiteService.update(id, dto);
        return ResponseEntity.ok(activite);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        activiteService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Activité supprimée"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activite> findById(@PathVariable Long id) {
        return ResponseEntity.ok(activiteService.findById(id));
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<Activite>> findByProjet(@PathVariable Long projetId) {
        return ResponseEntity.ok(activiteService.findByProjetId(projetId));
    }
    @GetMapping("/projet/{projetId}/email/{email}")
    public ResponseEntity<List<Activite>> findByProjetAndEMail(@PathVariable Long projetId,@PathVariable String email) {
        return ResponseEntity.ok(activiteService.findByProjetIdAndEmail(projetId,email));
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<Page<Activite>> findByEmail(@PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(activiteService.findByEmail(email,pageable));
    }
    @GetMapping("/email/{email}/dto-page")
    public ResponseEntity<Page<BaseEntityDto>> findDtoActiveByEmailPage(@PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(activiteService.findDtoActiveByEmail(email,pageable));
    }
    @GetMapping("/email/{email}/dto-list")
    public ResponseEntity<List<BaseEntityDto>> findDtoActiveByEmailList(@PathVariable String email) {
        return ResponseEntity.ok(activiteService.findDtoActiveByEmail(email));
    }

    @PostMapping("/{id}/fichiers")
    public ResponseEntity<?> addFichier(@PathVariable Long id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "nom", required = false) String nom) {
        activiteService.addFichier(id, new FichierDTO(file, nom));
        return ResponseEntity.ok(new ApiResponse("Fichier ajouté"));
    }

    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<?> deleteFichier(@PathVariable Long id, @PathVariable Long fichierId) {
        activiteService.deleteFichier(id, fichierId);
        return ResponseEntity.ok(new ApiResponse("Fichier supprimé"));
    }

    @PatchMapping("/{id}/designation")
    public ResponseEntity<Activite> updateDesignation(@PathVariable Long id, @RequestBody TexteDto dto) {
        Activite activite = activiteService.updateDesignation(id, dto);
        return ResponseEntity.ok(activite);
    }
    @PatchMapping("/{id}/description")
    public ResponseEntity<Activite> updateDescription(@PathVariable Long id, @RequestBody TexteDto dto) {
        Activite activite = activiteService.updateDescription(id, dto);
        return ResponseEntity.ok(activite);
    }
    @PatchMapping("/{id}/date-debut")
    public ResponseEntity<Activite> updateDateDebut(@PathVariable Long id, @RequestBody DateDto dto) {
        Activite activite = activiteService.updateDateDebut(id, dto);
        return ResponseEntity.ok(activite);
    }
    @PatchMapping("/{id}/date-fin")
    public ResponseEntity<Activite> updateDateFin(@PathVariable Long id, @RequestBody DateDto dto) {
        Activite activite = activiteService.updateDateFin(id, dto);
        return ResponseEntity.ok(activite);
    }


}