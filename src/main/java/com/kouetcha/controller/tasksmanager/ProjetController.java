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

@RestController
@CrossOrigin(origins = "${client.url}")
@RequestMapping("projets")
@Tag(name = "Projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;



    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Projet> create(@ModelAttribute BaseEntityGestionDto dto) {
        Projet projet = projetService.create(dto);
        return ResponseEntity.ok(projet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Projet> update(@PathVariable Long id, @RequestBody BaseEntityGestionDto dto) {
        Projet projet = projetService.update(id, dto);
        return ResponseEntity.ok(projet);
    }
    @PatchMapping("/{id}/designation")
    public ResponseEntity<Projet> updateDesignation(@PathVariable Long id, @RequestBody TexteDto dto) {
        Projet projet = projetService.updateDesignation(id, dto);
        return ResponseEntity.ok(projet);
    }
    @PatchMapping("/{id}/description")
    public ResponseEntity<Projet> updateDescription(@PathVariable Long id, @RequestBody TexteDto dto) {
        Projet projet = projetService.updateDescription(id, dto);
        return ResponseEntity.ok(projet);
    }
    @PatchMapping("/{id}/date-debut")
    public ResponseEntity<Projet> updateDateDebut(@PathVariable Long id, @RequestBody DateDto dto) {
        Projet projet = projetService.updateDateDebut(id, dto);
        return ResponseEntity.ok(projet);
    }
    @PatchMapping("/{id}/date-fin")
    public ResponseEntity<Projet> updateDateFin(@PathVariable Long id, @RequestBody DateDto dto) {
        Projet projet = projetService.updateDateFin(id, dto);
        return ResponseEntity.ok(projet);
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        projetService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Projet supprimé"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Projet> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projetService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Projet>> findAll() {
        return ResponseEntity.ok(projetService.findAll());
    }

    @PostMapping("/{id}/fichiers")
    public ResponseEntity<?> addFichier(@PathVariable Long id,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "nom", required = false) String nom) {
        projetService.addFichier(id, new FichierDTO(file, nom));
        return ResponseEntity.ok(new ApiResponse("Fichier ajouté"));
    }

    @DeleteMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<?> deleteFichier(@PathVariable Long id, @PathVariable Long fichierId) {
        projetService.deleteFichier(id, fichierId);
        return ResponseEntity.ok(new ApiResponse("Fichier supprimé"));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Page<Projet>> findByEmail(@PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(projetService.findByEmail(email,pageable));
    }
    @GetMapping("/email/{email}/dto-page")
    public ResponseEntity<Page<BaseEntityDto>> findDtoActiveByEmailPage(@PathVariable String email, Pageable pageable) {
        return ResponseEntity.ok(projetService.findDtoActiveByEmail(email,pageable));
    }
    @GetMapping("/email/{email}/dto-list")
    public ResponseEntity<List<BaseEntityDto>> findDtoActiveByEmailList(@PathVariable String email) {
        return ResponseEntity.ok(projetService.findDtoActiveByEmail(email));
    }

}
