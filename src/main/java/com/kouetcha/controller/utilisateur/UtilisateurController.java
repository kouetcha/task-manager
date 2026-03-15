package com.kouetcha.controller.utilisateur;

import com.kouetcha.dto.utilisateur.*;

import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.service.utilisateur.UtilisateurService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@CrossOrigin(origins = "${client.url}")
@RequestMapping("utilisateur")
@RequiredArgsConstructor
@Tag(name = "Utilisateur")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;


    @PostMapping("create")
    public ResponseEntity<Utilisateur> create(@RequestBody @Valid UtilisateurDto dto) {
        Utilisateur utilisateur = utilisateurService.create(dto);
        return ResponseEntity.ok(utilisateur);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Utilisateur> update(
            @PathVariable Long id,
            @RequestBody @Valid UtilisateurUpdateDto dto) {
        Utilisateur utilisateur = utilisateurService.update(id, dto);
        return ResponseEntity.ok(utilisateur);
    }
    @PatchMapping("/connexion")
    public ResponseEntity<AuthentificationDto> seConnecter(
            @RequestBody @Valid ConnexionDto dto) {
        AuthentificationDto authentificationDto = utilisateurService.seConnecter( dto);
        return ResponseEntity.ok(authentificationDto);
    }


    @PatchMapping("/{id}/etat")
    public ResponseEntity<Utilisateur> changeEtat(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.changeEtat(id);
        return ResponseEntity.ok(utilisateur);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/motdepasse")
    public ResponseEntity<Utilisateur> updatePassword(@RequestBody @Valid MotdePasseDto dto) {
        Utilisateur utilisateur = utilisateurService.updatePassWord(dto);
        return ResponseEntity.ok(utilisateur);
    }


    @GetMapping
    public ResponseEntity<List<Utilisateur>> findAll() {
        List<Utilisateur> utilisateurs = utilisateurService.findAll();
        return ResponseEntity.ok(utilisateurs);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> findById(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.findById(id);
        return ResponseEntity.ok(utilisateur);
    }


    @GetMapping("/email/{email}")
    public ResponseEntity<Utilisateur> findByEmail(@PathVariable String email) {
        Utilisateur utilisateur = utilisateurService.findByEmail(email);
        return ResponseEntity.ok(utilisateur);
    }
    // ✅ Récupérer la photo de profil d'un utilisateur
    @GetMapping("/image/{fileCode}")
    public ResponseEntity<Resource> recupererImage(
            @PathVariable String fileCode) {
        return utilisateurService.recupererImage(fileCode);
    }
    @PatchMapping("/{id}/profil-picture")
    public ResponseEntity<Utilisateur> modifierProfilPicture(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        Utilisateur utilisateur= utilisateurService.modifierProfilPicture(id, image);
        return ResponseEntity.ok(utilisateur);
    }
}